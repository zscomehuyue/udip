package com.alibaba.otter.common.push.index.wide.config;

import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.DynmaicIndexService;
import com.alibaba.otter.shared.common.utils.NameFormatUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.otter.shared.common.utils.LogUtils.WARN;
import static com.alibaba.otter.shared.common.utils.LogUtils.log;

public class RegistStageDynmaicIndexService implements InitializingBean, BeanFactoryAware, DynmaicIndexService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IndexConfigServiceFactory indexConfigServiceFactory;
    private IndexService indexService;
    private BeanFactory beanFactory;

    String SPLIT_SUFFIX = "_";

    @Override
    public Map<String, Map<String, Map<String, Object>>> getDynmaicDataMap(String index, String type, OperateType eventType, Map<String, Map<String, Object>> pkidMap) {
        Map<String, Map<String, Map<String, Object>>> indexMaps = Maps.newHashMap();
        pkidMap.forEach((k, v) -> {
            String indexKey = getIndexKey(index, v);
            indexConfigServiceFactory.createIndexWithMapping(index, indexKey, indexService.getClient());
            indexMaps.putIfAbsent(indexKey, new HashMap<>());
            indexMaps.get(indexKey).put(k, v);
        });

        return indexMaps;
    }

    private String getIndexKey(String index, Map<String, Object> v) {
        String dynamicIndex = index + SPLIT_SUFFIX + v.get(RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_CITY_ID_NAME) + SPLIT_SUFFIX + v.get(RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_YEAR_NAME);
        log(WARN, logger, () -> "=getIndexKey=>index:%s", dynamicIndex);
        return dynamicIndex;
    }

    @Override
    public Map<String, List<String>> getDynmaicDataMap(String index, String type, List<String> pkIds) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(RegistStageWideIndexConstants.REGIST_STAGE_PKID_NAME, pkIds));
        List<String> fetchFields = Lists.newArrayList(RegistStageWideIndexConstants.REGIST_STAGE_PKID_NAME);
        fetchFields.addAll(getIndexConfigServiceFactory().getTableNeedField(NameFormatUtils.formatName(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME)));
        Map<String, Map<String, Object>> pkidDataMap = getIndexService().getDataMapByIds(index, type, queryBuilder, fetchFields, RegistStageWideIndexConstants.REGIST_STAGE_PKID_NAME);
        HashMap<String, List<String>> indexDataMap = Maps.newHashMap();
        pkidDataMap.forEach((k, v) -> {
            indexDataMap.putIfAbsent(getIndexKey(index, v), Lists.newArrayList());
            indexDataMap.get(getIndexKey(index, v)).add((String) v.get(RegistStageWideIndexConstants.REGIST_STAGE_PKID_NAME));
        });
        return indexDataMap;
    }

    public IndexConfigServiceFactory getIndexConfigServiceFactory() {
        if (indexConfigServiceFactory == null) {
            indexConfigServiceFactory = beanFactory.getBean(IndexConfigServiceFactory.class);
        }
        return indexConfigServiceFactory;
    }

    public IndexService getIndexService() {
        if (null == indexService) {
            indexService = beanFactory.getBean(IndexService.class);
        }
        return indexService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        indexService = beanFactory.getBean(IndexService.class);
        indexConfigServiceFactory = beanFactory.getBean(IndexConfigServiceFactory.class);
    }

    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
