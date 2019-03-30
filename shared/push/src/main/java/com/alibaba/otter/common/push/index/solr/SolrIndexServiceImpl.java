package com.alibaba.otter.common.push.index.solr;

import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.shared.common.page.PageList;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.util.List;
import java.util.Map;

public class SolrIndexServiceImpl implements IndexService {

    @Override
    public void createIndex(String indexName) {

    }

    @Override
    public void deleteById(String index, String type, String id) {

    }

    @Override
    public void saveById(String index, String type, String id, Map<String, Object> dataMap) {

    }

    @Override
    public void mergeById(String index, String type, String id, Map<String, Object> dataMap) {

    }

    @Override
    public boolean updateById(String index, String type, String id, Map<String, Object> dataMap) {
        return false;
    }

    @Override
    public int[] batchDeleteByIds(String index, String type, List<String> ids, Map<String, List<String>> dynamicIds) {
        return new int[0];
    }

    @Override
    public int[] batchSaveByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, Map<String, Map<String, Map<String, Object>>> dynamicMap) {
        return new int[0];
    }

    @Override
    public int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, Map<String, Map<String, Map<String, Object>>> dynamicMap) {
        return new int[0];
    }

    @Override
    public int[] batchUpdateByDynamicIndex(String index, String type, Map<String, Map<String, Map<String, Object>>> dynamicMap) {
        return new int[0];
    }

    @Override
    public int[] batchSaveByDynamicIndex(String index, String type, Map<String, Map<String, Map<String, Object>>> dynamicMap) {
        return new int[0];
    }

    @Override
    public int[] batchDelByDynamicIndex(String index, String type, Map<String, List<String>> dynamicMap) {
        return new int[0];
    }

    @Override
    public int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, boolean commit) {
        return new int[0];
    }

    @Override
    public Object getIndexObject(int mysqlType, String data) {
        return null;
    }

    @Override
    public int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap) {
        return new int[0];
    }

    @Override
    public Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, String pkIdName) {
        return null;
    }

    @Override
    public Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, List<String> excludeFields, String pkIdName) {
        return null;
    }

    @Override
    public PageList<Map<String, Object>> getDataByPage(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public PageList<Map<String, Object>> getDataByPage(String index, String type, BoolQueryBuilder queryBuilder, FieldSortBuilder[] orders, List<String> fetchFields, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public int getCount(String index, String type, BoolQueryBuilder builder) {
        return 0;
    }

    @Override
    public int[] getInitBySize(int size) {
        return new int[0];
    }

    @Override
    public List<String> getExistIds(String index, String type, String pkidName, List<String> ids) {
        return null;
    }

    @Override
    public List<String> getExistIds(String index, String type, String pkidName, BoolQueryBuilder queryBuilder, List<String> ids) {
        return null;
    }

    @Override
    public TransportClient getClient() {
        return null;
    }

    @Override
    public void destroy() throws Exception {

    }
}
