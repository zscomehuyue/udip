package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.common.push.index.es.EsIndexServiceImpl;
import com.alibaba.otter.common.push.index.es.MappingServiceImpl;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.page.PageList;
import com.alibaba.otter.shared.common.utils.DateUtils;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.etl.model.EventData;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.Assert;

import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.client.Requests.refreshRequest;

public class EventDataIndexServiceTest {
    static EsIndexServiceImpl client = new EsIndexServiceImpl();
    static EventDataIndexService service = new EventDataIndexService();

    static {
        //http://192.168.13.186:9200/
//        client.setClusterName("ES-ClassList");
        client.setClusterName("es");
        client.setClusterNodes("10.200.0.109:9300");
//        client.setClusterNodes("192.168.13.186:9300");
        client.buildClient();
        MappingServiceImpl mappingService = new MappingServiceImpl();
        try {
            mappingService.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.setMappingService(mappingService);
        service.setIndexService(client);

    }

    //    static void updateByNotPkId(){
//        TransportClient client = EventDataIndexServiceTest.client.getClient();
////        UpdateRequest request = new UpdateRequest();
//        Map<Object, Object> map = Maps.newHashMap();
//        client.prepareUpdate().setDoc(map);
//    }

    static void refresh(String indexName) {
        Assert.notNull(indexName, "No index defined for refresh()");
        client.getClient().admin().indices().refresh(refreshRequest(indexName)).actionGet();
    }

    static void index() {
        MappingServiceImpl mappingService = new MappingServiceImpl();
        try {
            mappingService.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> dataMap = new MapMaker().makeMap();
        dataMap.put("id", "1");
        dataMap.put("timeStr", DateUtils.nowStr());
        Date value = new Date(DateUtils.nowSecond() * 1000);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dataMap.put("Datetime", value);
        String format1 = format.format(value);
        System.out.println("format1=" + format1);
        dataMap.put("Datetime_format", format1);
        Object foramt = mappingService.getEsObject(Types.DATE, format1);
        dataMap.put("foramt", foramt);
        client.saveById("repair_test", "udip", "1", dataMap);
        HashMap map = client.getDataById("test_1", "udip", "1", HashMap.class);
        //refresh("repair_test");
        if (null != map) {
            System.out.println("get=" + map.get("timeStr"));
        } else {
            System.out.println("not get=");
        }

    }


    /**
     * if (null == param.get("clazz_id")
     * || null == param.get("classRegistCount_id")
     * || null == param.get("classtime_id")
     * || null == param.get("department_id")
     * || null == param.get("classtimeType_id")
     * || null == param.get("classlevel_id"))
     */
    static void query() {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
//        builder.mustNot(QueryBuilders.existsQuery("clazz_id"));
//        builder.mustNot(QueryBuilders.existsQuery("classRegistCount_id"));
//        builder.mustNot(QueryBuilders.existsQuery("classtime_id"));
//        builder.mustNot(QueryBuilders.existsQuery("department_id"));
//        builder.mustNot(QueryBuilders.existsQuery("classtimeType_id"));
//        builder.mustNot(QueryBuilders.existsQuery("classlevel_id"));
        builder.must(QueryBuilders.termsQuery("classtimeType_id.keyword", new ArrayList<String>() {{
            add("cuc_classtimeType_id_1468");
        }}));
        ArrayList<String> list = Lists.newArrayList();
        list.add("clazz_id");
        list.add("classRegistCount_id");
        list.add("classtime_id");
        list.add("department_id");
        list.add("classtimeType_id");
        list.add("classlevel_id");
        Map<String, Map<String, Object>> map = client.getDataMapByIds("classtimetype", "udip", builder, null, "classtimeType_id");
        System.out.println(map.size());
    }

    static void wildcardQuery() {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
//        builder.must(QueryBuilders.wildcardQuery("curriculum_id", "*6*"));
        builder.must(QueryBuilders.wildcardQuery("curriculum_id", "*dirty*"));
        PageList<Map<String, Object>> page = client.getDataByPage("curriculum", "udip", builder, null, 0, 10);
        Optional.ofNullable(page).ifPresent(mapp -> {
            page.getList().forEach(map -> {
                System.out.println("curriculum_id=" + map.get("curriculum_id"));
                System.out.println(JsonUtils.marshalToString(map));

            });
        });
    }

//    static Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, String pkIdName) {
//        int size = 100;
//        SearchRequestBuilder request = client.getClient().prepareSearch(index).setTypes(type);
//        if (null != fetchFields) {
//            request.setFetchSource(true).setFetchSource(fetchFields.toArray(new String[fetchFields.size()]), new String[]{});
//        }
//        SearchResponse response = request.setQuery(queryBuilder).setFrom(0).setSize(size).setExplain(false).execute().actionGet();
//        if (null != response) {
//            if (response.getHits().getTotalHits() > size) {
//            }
//            Map<String, Map<String, Object>> resultMap = Arrays.stream(response.getHits().getHits())
////                    .filter(searchHit -> null != searchHit.getSourceAsMap())
//                    .map(SearchHit::getSourceAsMap).collect(Collectors.toMap(map -> map.get(pkIdName).toString()
//                    , map -> {
//                        if (null == fetchFields) {
//                            return map;
//                        }
//                        Map<String, Object> result = new HashMap<String, Object>();
//                        fetchFields.forEach(s -> {
//                            result.put(s, map.get(s));
//                        });
//                        return result;
//                    }
//            ));
//            return resultMap;
//        }
//        return new HashMap<>();
//    }


    static void queryByUpdate() {
        List<EventData> slaveList = new ArrayList<>();
        WideTable wideTable = new WideTable();
        wideTable.setSlaveTableFkIdName("clazz_id");
        wideTable.setMainTablePkIdName("curriculum_id");
        wideTable.setMainTableFkIdName("curriculum_classId");

//        service.queryByUpdate("curriculum", "udip", slaveList, wideTable);
    }

    static void getExists() {

//        ArrayList<String> ids = Lists.newArrayList("ff80808162c297490162cd8c113c71b7", "ff80808162c297490162cd8c114b71bd", "ff80808162c297490162cd8c26f7734f-2222");
//        List<String> list = client.getExistIds("curriculum", "udip", "curriculum_id", ids);
//        System.out.println("es-size=" + list.size());
//        System.out.println("para-ids=" + ids.size());
//        ids.removeAll(list);
//
//        System.out.println("para-=-ids=" + ids.size());
    }

    static void testCount() {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.termsQuery("clazz_year", "2018", "2019"));
        builder.must(QueryBuilders.termQuery("curriculum_cityId", "021"));
        builder.must(QueryBuilders.rangeQuery("curriculum_modifyTime").gte("2018-12-05T00:00:00").lte("2018-12-15T00:00:00"));
        int pageSize = 1000;
        ArrayList<java.lang.String> includeFields = Lists.newArrayList("curriculum_id", "clazz_maxPersons", "remainCount"
                , "classRegistCount_registCount", "curriculum_changeoutCourseNum", "curriculum_changeinCourseNum");

        PageList<Map<java.lang.String, Object>> page = client.getDataByPage("curriculum", "udip", builder
                , null, includeFields, 0, pageSize);
        int totalPage = page.getTotalPage();
        System.out.println("totalPage=" + totalPage);
        System.out.println("getTotal=" + page.getTotal());
        for (int i = 0; i <= totalPage; i++) {
            page = client.getDataByPage("curriculum", "udip", builder
                    , null, includeFields, i, pageSize);
            page.getList().forEach(map -> {
                if (map.get("curriculum_id").equals("ff80808165f043280165f553139a301a")) {
                    int value = Integer.parseInt(map.get("clazz_maxPersons").toString())
                            - Integer.parseInt(map.get("classRegistCount_registCount").toString())
                            + Integer.parseInt(map.get("curriculum_changeoutCourseNum").toString())
                            - Integer.parseInt(map.get("curriculum_changeinCourseNum").toString());
                    System.err.println(value + "==" + Integer.parseInt(map.get("remainCount").toString()));
                }
            });
            System.out.println(i + "=" + page.getList().size());
        }

    }

    static void getById() {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.termsQuery("clazz_year", "2018", "2019"));
        builder.must(QueryBuilders.termQuery("curriculum_id", "ff80808165f043280165f553139a301a"));
        int pageSize = 1000;
        ArrayList<java.lang.String> includeFields = Lists.newArrayList("curriculum_id", "clazz_maxPersons", "remainCount"
                , "classRegistCount_registCount", "curriculum_changeoutCourseNum", "curriculum_changeinCourseNum");

        PageList<Map<java.lang.String, Object>> page = client.getDataByPage("curriculum", "udip", builder
                , null, includeFields, 0, pageSize);
        page.getList().forEach(map -> {
            if (map.get("curriculum_id").equals("ff80808165f043280165f553139a301a")) {
                int value = Integer.parseInt(map.get("clazz_maxPersons").toString())
                        - Integer.parseInt(map.get("classRegistCount_registCount").toString())
                        + Integer.parseInt(map.get("curriculum_changeoutCourseNum").toString())
                        - Integer.parseInt(map.get("curriculum_changeinCourseNum").toString());
                System.err.println(value + "==" + Integer.parseInt(map.get("remainCount").toString()));
            }
        });

    }

    public static void main(String[] args) {
//        setUp();
//         create();
//        getDataById();
//        index();
//        wildcardQuery();
        index();
//        testCount();
//        getById();

    }


}
