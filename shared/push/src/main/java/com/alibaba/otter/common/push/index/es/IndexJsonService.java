package com.alibaba.otter.common.push.index.es;


import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;

import java.util.List;
import java.util.Map;

public interface IndexJsonService {

    void createIndex(String indexName);

    <T> T getDataById(String index, String type, String id, Class<T> clazz);

    <T> List<T> getDataList(String indexName, String indexType, int pageSize
            , BoolQueryBuilder queryBuilder
            , FieldSortBuilder[] orders
            , Class<T> clazz
            , String... includeFileds);

    boolean updateById(String index, String type, String id, String json);

    void batchUpdateByIdJson(String index, String type, Map<String, String> idJsonMap);

    void mergeById(String index, String type, String id, String json);

    void batchSaveByIdJson(String index, String type, Map<String, String> idDataMap);
}
