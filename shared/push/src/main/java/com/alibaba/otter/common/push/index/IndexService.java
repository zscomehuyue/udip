package com.alibaba.otter.common.push.index;


import com.alibaba.otter.shared.common.page.PageList;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.Map;

public interface IndexService extends DisposableBean {

    void createIndex(String indexName);

    void deleteById(String index, String type, String id);

    void saveById(String index, String type, String id, Map<String, Object> dataMap);

    void mergeById(String index, String type, String id, Map<String, Object> dataMap);

    boolean updateById(String index, String type, String id, Map<String, Object> dataMap);

    int[] batchDeleteByIds(String index, String type, List<String> ids, Map<String, List<String>> dynamicIds);

    int[] batchSaveByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, Map<String, Map<String, Map<String, Object>>> dynamicMap);

    int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, Map<String, Map<String, Map<String, Object>>> dynamicMap);

    int[] batchUpdateByDynamicIndex(String index, String type, Map<String, Map<String, Map<String, Object>>> dynamicMap);

    int[] batchSaveByDynamicIndex(String index, String type, Map<String, Map<String, Map<String, Object>>> dynamicMap);

    int[] batchDelByDynamicIndex(String index, String type, Map<String, List<String>> dynamicMap);

    int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap, boolean commit);

    Object getIndexObject(int mysqlType, String data);


    int[] batchUpdateByIds(String index, String type, Map<String, Map<String, Object>> idDataMap);

    /**
     * 该index主键Id对应的 ，对应的数值（给定的字段 if not null）；
     *
     * @param index
     * @param type
     * @param queryBuilder
     * @param fetchFields
     * @param pkIdName
     * @return
     */
    Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, String pkIdName);

    Map<String, Map<String, Object>> getDataMapByIds(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, List<String> excludeFields, String pkIdName);

    PageList<Map<String, Object>> getDataByPage(String index, String type, BoolQueryBuilder queryBuilder, List<String> fetchFields, int pageNo, int pageSize);

    PageList<Map<String, Object>> getDataByPage(String index, String type, BoolQueryBuilder queryBuilder, FieldSortBuilder[] orders, List<String> fetchFields, int pageNo, int pageSize);
//    int getCount(String index, String type, String timeField, String beginTime, String EndTime);

    int getCount(String index, String type, BoolQueryBuilder builder);

    int[] getInitBySize(int size);

    List<String> getExistIds(String index, String type, String pkidName, List<String> ids);

    List<String> getExistIds(String index, String type, String pkidName, BoolQueryBuilder queryBuilder, List<String> ids);

    TransportClient getClient();
}
