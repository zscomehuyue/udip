package com.alibaba.otter.common.push.index.es;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.shared.common.page.PageList;
import com.alibaba.otter.shared.common.page.PaginatedList;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.otter.shared.common.utils.LogUtils.ERROR;
import static com.alibaba.otter.shared.common.utils.LogUtils.INFO;
import static org.elasticsearch.client.Requests.refreshRequest;

public class EsIndexServiceImpl implements IndexService, IndexJsonService {
    private static final Logger log = LoggerFactory.getLogger(EsIndexServiceImpl.class);
    private String clusterName;
    private String clusterNodes;
    private TransportClient client;
    private IMappingService mappingService;

    public EsIndexServiceImpl buildClient() {
        Preconditions.checkNotNull(clusterName, "cluster name is not null.");
        Preconditions.checkNotNull(clusterNodes, "cluster nodes is not null.");
        Settings settings = Settings.builder()
                .put("cluster.name", StringUtils.deleteWhitespace(clusterName))
                .put("client.transport.sniff", true)
                .build();
        client = new PreBuiltTransportClient(settings);
        for (String node : StringUtils.deleteWhitespace(clusterNodes).split(",")) {
            String[] ipports = StringUtils.deleteWhitespace(node).split(":");
            Preconditions.checkNotNull(ipports, "node must contain suffix : .");
            Preconditions.checkState(ipports.length == 2, "ip and port must be config.");
            client.addTransportAddress(new TransportAddress(new InetSocketAddress(ipports[0], Integer.valueOf(ipports[1]))));
        }
        return this;
    }

    public void createIndex(String indexName) {
        if (!client.admin().indices().prepareExists(indexName).get().isExists()) {
            client.admin().indices().prepareCreate(indexName).get();
        }
    }

    @Retryable(value = Exception.class, backoff = @Backoff(value = 0L))
    public void saveById(String index, String type, String id, String json) {
        client.prepareIndex(index, type, id).setSource(json, XContentType.JSON).get();
    }

    @Override
    public void mergeById(String index, String type, String id, String json) {
        if (existById(index, type, id)) {
            updateById(index, type, id, json);
        } else {
            saveById(index, type, id, json);
        }
    }

    @Retryable(value = Exception.class, backoff = @Backoff(value = 0L))
    public void saveById(String index, String type, String id, Map<String, Object> dataMap) {
        client.prepareIndex(index, type, id).setSource(dataMap).get();
    }

    @Override
    public void mergeById(String index, String type, String id, Map<String, Object> dataMap) {
        if (existById(index, type, id)) {
            updateById(index, type, id, dataMap);
        } else {
            saveById(index, type, id, dataMap);
        }
    }

    @Retryable(value = Exception.class, backoff = @Backoff(value = 0L))
    public boolean updateById(String index, String type, String id, Map<String, Object> dataMap) {
        try {
            UpdateResponse response = client.prepareUpdate(index, type, id).setDoc(dataMap).get();
            return response.status() == RestStatus.OK;
        } catch (DocumentMissingException e) {
            return false;
        }
    }

    @Retryable(value = Exception.class, backoff = @Backoff(value = 0L))
    public boolean updateById(String index, String type, String id, String json) {
        try {
            UpdateResponse response = client.prepareUpdate(index, type, id).setDoc(json, XContentType.JSON).get();
            return response.status() == RestStatus.OK;
        } catch (DocumentMissingException e) {
            return false;
        }
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public void batchUpdateByIdJson(String index, String type, Map<String, String> idJsonMap) {
        if (!CollectionUtils.isEmpty(idJsonMap)) {
            BulkRequestBuilder bulk = client.prepareBulk();
            idJsonMap.forEach((id, json) -> bulk.add(client.prepareUpdate(index, type, id).setDoc(json, XContentType.JSON)));
            try {
                BulkResponse response = bulk.execute().get();
                handelFails(response, index);
            } catch (Exception e) {
                log.error("=batchUpdateByIdJson=>error: index=" + index + ", type=" + type + ", data=" + ToStringBuilder.reflectionToString(idJsonMap), e);
                throw new EsException(e);
            }
        }
    }


    @Retryable(value = Exception.class, backoff = @Backoff(value = 0L))
    public void deleteById(String index, String type, String id) {
        client.prepareDelete(index, type, id).get();
    }

    public Map<String, Map<String, Object>> getDatasByIds(String index, String type, Collection<String> ids, String pkidName) {
        MultiGetRequestBuilder multiGet = client.prepareMultiGet();
        ids.stream().forEach(id -> multiGet.add(index, type, id));
        MultiGetResponse responses = multiGet.execute().actionGet();
        return Arrays.stream(responses.getResponses()).map(response -> response.getResponse().getSourceAsMap())
                .collect(Collectors.toMap(map -> map.get(pkidName).toString(), map -> map, (t1, t2) -> t1));
    }

    public boolean existById(String index, String type, String id) {
        GetResponse response = client.prepareGet(index, type, id).execute().actionGet();
        return null == response ? false : (null == response.getSourceAsMap() ? false : response.getSourceAsMap().size() > 0);
    }

    public <T> T getDataById(String index, String type, String id, Class<T> clazz) {
        GetResponse response = client.prepareGet(index, type, id).execute().actionGet();
        return JsonUtils.unmarshalFromString(response.getSourceAsString(), clazz);
    }

    @Override
    public Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, String pkIdName) {
        return getDataMapByIds(index, type, queryBuilder, fetchFields, null, pkIdName);
    }

    public Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, List<String> excludeFields, String pkIdName) {
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type);
        if (!CollectionUtils.isEmpty(fetchFields) || !CollectionUtils.isEmpty(excludeFields)) {
            if (StringUtils.isNotEmpty(pkIdName) && !CollectionUtils.isEmpty(fetchFields) && !fetchFields.contains(pkIdName)) {
                fetchFields.add(pkIdName);
            }
            request.setFetchSource(true).setFetchSource(CollectionUtils.isEmpty(fetchFields) ? new String[]{} : fetchFields.toArray(new String[fetchFields.size()])
                    , CollectionUtils.isEmpty(excludeFields) ? new String[]{} : excludeFields.toArray(new String[excludeFields.size()]));
        }
        int count = getCount(index, type, queryBuilder);
        LogUtils.log(INFO, log, () -> "=getDataMapByIds=>count:%s", count);
        SearchResponse response = request.setQuery(queryBuilder).setFrom(0).setSize(count).setExplain(false).execute().actionGet();
        if (null != response) {
            Map<String, Map<String, Object>> resultMap = Arrays.stream(response.getHits().getHits()).map(SearchHit::getSourceAsMap).collect(Collectors.toConcurrentMap(map -> map.get(pkIdName) == null ? System.currentTimeMillis() + "_dirty" : map.get(pkIdName).toString()
                    , map -> {
                        if (CollectionUtils.isEmpty(fetchFields)) {
                            return map;
                        }
                        Map<String, Object> result = new ConcurrentHashMap<>();
                        fetchFields.forEach(s -> {
                            if (null != map.get(s)) {
                                result.put(s, map.get(s));
                            }
                        });
                        return result;
                    }, (d1, d2) -> d1
            ));
            return resultMap;
        }
        return new HashMap<>();
    }

    public PageList<Map<String, Object>> getDataByPage(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, int pageNo, int pageSize) {
        return getDataByPage(index, type, queryBuilder, null, fetchFields, pageNo, pageSize);
    }

    public PageList<Map<String, Object>> getDataByPage(String index, String type, BoolQueryBuilder queryBuilder, FieldSortBuilder[] orders, List<String> fetchFields, int pageNo, int pageSize) {
        PageList<Map<String, Object>> page = new PaginatedList();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        page.setTotal(getCount(index, type, queryBuilder));
        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type);
        if (null != fetchFields) {
            request.setFetchSource(true).setFetchSource(fetchFields.toArray(new String[fetchFields.size()]), new String[]{});
        }
        if (null != orders) {
            Arrays.stream(orders).forEach(order -> request.addSort(order));
        }
        SearchResponse response = request.setQuery(queryBuilder).setFrom(page.getStartRow()).setSize(page.getPageSize()).setExplain(false).execute().actionGet();
        if (null != response) {
            List<Map<String, Object>> list = Arrays.stream(response.getHits().getHits()).map(SearchHit::getSourceAsMap).collect(Collectors.toList());
            page.addAll(list);
            page.setTotal((int) response.getHits().getTotalHits());
        }
        return page;
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchSaveByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, Map<String, Map<String, Map<String, Object>>> dynamicIndexdMap) {
        if (null != dynamicIndexdMap) {
            return batchSaveByDynamicIndex(index, type, dynamicIndexdMap);
        }
        return batchSaveByIds(index, type, idDataMap);
    }


    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchSaveByIds(String index, String type, Map<String, Map<String, Object>> idDataMap) {
        if (MapUtils.isEmpty(idDataMap)) {
            return new int[0];
        }
        BulkRequestBuilder bulk = client.prepareBulk();
        idDataMap.forEach((id, dataMap) -> bulk.add(client.prepareIndex(index, type, id).setSource(dataMap)));
        try {
            bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            BulkResponse response = bulk.execute().get();
            handelFails(response, index);
            return getAffects(response, idDataMap.size());
        } catch (Exception e) {
            log.error("=batchSaveByIds=>error: index=" + index + ", type=" + type + ", data=" + ToStringBuilder.reflectionToString(idDataMap), e);
            throw new EsException(e);
        }
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public void batchSaveByIdJson(String index, String type, Map<String, String> idDataMap) {
        BulkRequestBuilder bulk = client.prepareBulk();
        idDataMap.forEach((id, json) -> bulk.add(client.prepareIndex(index, type, id).setSource(json, XContentType.JSON)));
        bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            BulkResponse response = bulk.execute().get();
            handelFails(response, index);
        } catch (Exception e) {
            log.error("=batchSaveByIdJson=>error: index=" + index + ", type=" + type + ", data=" + ToStringBuilder.reflectionToString(idDataMap), e);
            throw new EsException(e);
        }
    }

    public void refresh(String indexName) {
        client.admin().indices().refresh(refreshRequest(indexName)).actionGet();
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap) {
        if (MapUtils.isEmpty(idDataMap)) {
            return new int[0];
        }
        BulkRequestBuilder bulk = client.prepareBulk();
        idDataMap.forEach((id, dataMap) -> bulk.add(client.prepareUpdate(index, type, id).setDoc(dataMap)));
        try {
            bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            BulkResponse response = bulk.execute().get();
            handelFails(response, index);
            return getAffects(response, idDataMap.size());
        } catch (Exception e) {
            log.error("=batchUpdateByIds=>error: index=" + index + ", type=" + type + ", datas=" + ToStringBuilder.reflectionToString(idDataMap), e);
            throw new EsException(e);
        }
    }


    private void handelFails(BulkResponse bulkResponse, String index) {
        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<>();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed()) {
                    failedDocuments.put(item.getId(), item.getFailureMessage());
                }
            }
            String errorMsg = LogUtils.format("=handelFails=>index:%s ,fails.size:%s ,details:%s", index, failedDocuments.size(), failedDocuments.entrySet().stream().map(entry -> "id:" + entry.getKey() + ",msg:" + entry.getValue()).collect(Collectors.joining("\n")));
            LogUtils.log(ERROR, log, () -> errorMsg);
            //throw new EsException(errorMsg);
        }
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, Map<String, Map<String, Map<String, Object>>> dynamicIndexdMap) {
        if (null != dynamicIndexdMap) {
            return batchUpdateByDynamicIndex(index, type, dynamicIndexdMap);
        }
        return batchUpdateByIds(index, type, idDataMap);
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchUpdateByDynamicIndex(String index, String type, Map<String, Map<String, Map<String, Object>>> dynamicMap) {
        List<int[]> affects = Lists.newArrayList();
        dynamicMap.forEach((k, v) -> {
            int[] ints = batchUpdateByIds(k, type, v);
            affects.add(ints);
        });
        ArrayList<Integer> listInt = Lists.newArrayList();
        affects.forEach(arry -> Arrays.stream(arry).forEach(a -> listInt.add(a)));
        return listInt.stream().mapToInt(Integer::intValue).toArray();
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchSaveByDynamicIndex(String index, String type, Map<String, Map<String, Map<String, Object>>> dynamicMap) {
        List<int[]> affects = Lists.newArrayList();
        dynamicMap.forEach((k, v) -> {
            int[] ints = batchSaveByIds(k, type, v);
            affects.add(ints);
        });
        ArrayList<Integer> listInt = Lists.newArrayList();
        affects.forEach(arry -> Arrays.stream(arry).forEach(a -> listInt.add(a)));
        return listInt.stream().mapToInt(Integer::intValue).toArray();
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchDelByDynamicIndex(String index, String type, Map<String, List<String>> dynamicMap) {
        List<int[]> affects = Lists.newArrayList();
        dynamicMap.forEach((k, v) -> {
            int[] ints = batchDeleteByIds(index + "_" + k, type, v);
            affects.add(ints);
        });
        ArrayList<Integer> listInt = Lists.newArrayList();
        affects.forEach(arry -> Arrays.stream(arry).forEach(a -> listInt.add(a)));
        return listInt.stream().mapToInt(Integer::intValue).toArray();
    }

    public int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, boolean commit) {
        if (MapUtils.isEmpty(idDataMap)) {
            return new int[0];
        }
        BulkRequestBuilder bulk = client.prepareBulk();
        idDataMap.forEach((id, dataMap) -> bulk.add(client.prepareUpdate(index, type, id).setDoc(dataMap)));
        try {
            if (commit) {
                bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            }
            BulkResponse response = bulk.execute().get();
            handelFails(response, index);
            return getAffects(response, idDataMap.size());
        } catch (Exception e) {
            log.error("=batchUpdateByIds=>error: index=" + index + ", type=" + type + ", datas=" + ToStringBuilder.reflectionToString(idDataMap), e);
            throw new EsException(e);
        }
    }

    private int[] getAffects(BulkResponse response, int size) {
        if (null == response.getItems()) {
            return getInitBySize(size);
        }
        return Stream.of(response.getItems())
                .mapToInt(item -> (item.status() == RestStatus.OK
                        || item.status() == RestStatus.CREATED) ? 1 : 0)
                .toArray();
    }

    public int[] getInitBySize(int size) {
        int[] ints = new int[size];
        for (int i = 0; i < size; i++) {
            ints[i] = 0;
        }
        return ints;
    }

    @Override
    public Object getIndexObject(int mysqlType, String data) {
        return mappingService.getEsObject(mysqlType, data);
    }


    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchDeleteByIds(String index, String type, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new int[0];
        }
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        ids.forEach((id) -> bulkRequestBuilder.add(client.prepareDelete(index, type, id)));
        try {
            //bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            BulkResponse bulkResponse = bulkRequestBuilder.execute().get();
            if (bulkResponse.hasFailures()) {
                log.error("=batchDeleteByIds=>error : index=" + index + ", type=" + type + ", data=" + Arrays.toString(ids.toArray()) + ", cause:" + bulkResponse.buildFailureMessage());
                handelFails(bulkResponse, index);
            }
            return getAffects(bulkResponse, ids.size());
        } catch (Exception e) {
            log.error("=batchDeleteByIds=>error : index=" + index + ", type=" + type + ", data=" + Arrays.toString(ids.toArray()), e);
            throw new EsException(e);
        }
    }

    @Retryable(value = EsException.class, backoff = @Backoff(value = 0L))
    public int[] batchDeleteByIds(String index, String type, List<String> ids, Map<String, List<String>> dynamicIds) {
        if (null != dynamicIds) {
            return batchDelByDynamicIndex(index, type, dynamicIds);
        }
        return batchDeleteByIds(index, type, ids);
    }


    public <T> List<T> getDataList(String indexName, String indexType, int pageSize
            , BoolQueryBuilder queryBuilder
            , FieldSortBuilder[] orders
            , Class<T> clazz
            , String... includeFileds) {
        List<T> list = new ArrayList<>();
        SearchRequestBuilder request = client.prepareSearch(indexName).setTypes(indexType);
        if (null != orders) {
            Arrays.stream(orders).forEach(order -> request.addSort(order));
        }
        if (includeFileds.length > 0) {
            request.setFetchSource(true);
            request.setFetchSource(includeFileds, null);
        }
        SearchResponse response = request.setQuery(queryBuilder).setFrom(0)
                .setSize(pageSize).setExplain(false).execute().actionGet();
        Arrays.stream(response.getHits().getHits()).forEach(searchHit -> list.add(JsonUtils.unmarshalFromString(searchHit.getSourceAsString(), clazz)));
        return list;
    }


//    public int getCount(String index, String type, String timeField, String beginTime, String EndTime) {
//        int count = 0;
//        SearchRequestBuilder request = client.prepareSearch(index).setTypes(type);
//        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(timeField);
//        if (StringUtils.isNotBlank(beginTime)) {
//            // es 时间根式转换
//            beginTime = beginTime.replace(" ", "T");
//            rangeQueryBuilder.gt(beginTime);
//        }
//        if (StringUtils.isNotBlank(EndTime)) {
//            // es 时间根式转换
//            EndTime = EndTime.replace(" ", "T");
//            rangeQueryBuilder.lte(EndTime);
//        }
//        SearchResponse response = request.setQuery(rangeQueryBuilder).setSize(0).get();
//        JSONObject json = JSONObject.parseObject(response.toString());
//        JSONObject hits = json.getJSONObject("hits");
//        count = hits.getInteger("total");
//        return count;
//    }

    public int getCount(String index, String type, BoolQueryBuilder builder) {
        SearchResponse response = client.prepareSearch(index).setTypes(type).setQuery(builder).setSize(0).get();
        return JSONObject.parseObject(response.toString()).getJSONObject("hits").getInteger("total");
    }

    public List<String> getExistIds(String index, String type, String pkidName, List<String> ids) {
        SearchResponse response = client.prepareSearch(index).setTypes(type)
                .setFetchSource(true)
                .setFetchSource(pkidName, null)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery(pkidName, ids)))
                .setFrom(0).setSize(ids.size() * 2)
                .setExplain(false)
                .execute()
                .actionGet();
        if (null != response) {
            return Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .map(map -> map.get(pkidName) == null ? System.currentTimeMillis() + "_dirty" : map.get(pkidName).toString())
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public List<String> getExistIds(String index, String type, String pkidName, BoolQueryBuilder queryBuilder, List<String> ids) {
        SearchResponse response = client.prepareSearch(index).setTypes(type)
                .setFetchSource(true)
                .setFetchSource(pkidName, null)
                .setQuery(queryBuilder.must(QueryBuilders.termsQuery(pkidName, ids)))
                .setFrom(0).setSize(ids.size() * 2)
                .setExplain(false)
                .execute()
                .actionGet();
        if (null != response) {
            return Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .map(map -> map.get(pkidName) == null ? System.currentTimeMillis() + "_dirty" : map.get(pkidName).toString())
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public TransportClient getClient() {
        return client;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public void setMappingService(IMappingService mappingService) {
        this.mappingService = mappingService;
    }

    public void destroy() throws Exception {
        if (client != null) {
            client.close();
        }
    }

}
