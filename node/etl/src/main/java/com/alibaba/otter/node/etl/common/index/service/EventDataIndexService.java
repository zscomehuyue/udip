package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.canal.common.utils.JsonUtils;
import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.IEventDataIndexService;
import com.alibaba.otter.common.push.index.wide.config.FieldMapping;
import com.alibaba.otter.common.push.index.wide.config.IndexConfigServiceFactory;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.*;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.DateUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.common.utils.NameFormatUtils;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

public class EventDataIndexService implements IEventDataIndexService, ApplicationEventPublisherAware, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexService indexService;

    private ApplicationEventPublisher publisher;

    private ApplicationContext applicationContext;

    public static final String table_field_sufix = "_";

    private static final String NO_FIELD_KEY = "no_field_key";

    private ExecutorService service;

    private IndexConfigServiceFactory indexConfigServiceFactory;

    protected ConfigClientService configClientService;

    public EventDataIndexService() {
        service = new ThreadPoolExecutor(200, 200, 0L, TimeUnit.MILLISECONDS
                , new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("-master-wide-")
                , (r, e) -> {
            log(ERROR, logger, () -> {
                        return "=EventDataIndexService=>Thread pool is EXHAUSTED!" +
                                " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                                " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)!";
                    }, "-master-wide-", e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                    e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
        });
    }

    public void deleteById(String index, String type, EventData eventData, String pkidName) {
        if (null == eventData || CollectionUtils.isEmpty(eventData.getKeys()) || eventData.getKeys().size() != 1) {
            logger.warn("=deleteById=>schemaName=" + eventData.getSchemaName() + ",tableName=" + eventData.getTableName() + " no pkid.");
            return;
        }

        indexService.deleteById(index, type, null == pkidName ? eventData.getKeys().get(0).getColumnValue() : eventData.getKeys().stream()
                .filter(eventColumn -> pkidName.endsWith(NameFormatUtils.formatName(eventColumn.getColumnName())))
                .findFirst().orElse(eventData.getColumns().stream()
                        .filter(eventColumn -> pkidName.endsWith(NameFormatUtils.formatName(eventColumn.getColumnName())))
                        .findFirst().orElse(null)).getColumnValue());
    }

    @Override
    public void updateById(String index, String type, EventData eventData, String pkidName) {
        if (null == eventData || CollectionUtils.isEmpty(eventData.getKeys()) || eventData.getKeys().size() != 1) {
            logger.warn("=updateById=>schemaName=" + eventData.getSchemaName() + ",tableName=" + eventData.getTableName() + " no pkid.");
            return;
        }

        indexService.updateById(index, type, null == pkidName ? eventData.getKeys().get(0).getColumnValue() : eventData.getKeys().stream()
                        .filter(eventColumn -> pkidName.endsWith(NameFormatUtils.formatName(eventColumn.getColumnName())))
                        .findFirst().orElse(eventData.getColumns().stream()
                                .filter(eventColumn -> pkidName.endsWith(NameFormatUtils.formatName(eventColumn.getColumnName())))
                                .findFirst().orElse(null)).getColumnValue(),
                parseColumnsToMap(eventData.getTableName(), new ArrayList<EventColumn>() {{
                    addAll(eventData.getKeys());
                    addAll(eventData.getColumns());
                }}, EventType.UPDATE));
    }

    public void saveById(String index, String type, EventData eventData, String pkidName) {
        if (null == eventData || CollectionUtils.isEmpty(eventData.getKeys()) || eventData.getKeys().size() != 1) {
            logger.warn("=saveById=>schemaName=" + eventData.getSchemaName() + ",tableName=" + eventData.getTableName() + " no pkid.");
            return;
        }

        indexService.saveById(index, type, null == pkidName ? eventData.getKeys().get(0).getColumnValue() : eventData.getKeys().stream()
                        .filter(eventColumn -> pkidName.endsWith(NameFormatUtils.formatName(eventColumn.getColumnName())))
                        .findFirst().orElse(eventData.getColumns().stream()
                                .filter(eventColumn -> pkidName.endsWith(NameFormatUtils.formatName(eventColumn.getColumnName())))
                                .findFirst().orElse(null)).getColumnValue(),
                parseColumnsToMap(eventData.getTableName(), new ArrayList<EventColumn>() {{
                    addAll(eventData.getKeys());
                    addAll(eventData.getColumns());
                }}, EventType.INSERT));
    }


    public int[] batchDeleteByIds(String index, String type, List<EventData> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(list.get(0).getKeys()) || list.get(0).getKeys().size() != 1) {
            logger.warn("=batchDeleteByIds=>schemaName=" + list.get(0).getSchemaName() + ",tableName=" + list.get(0).getTableName() + " no pkid.");
            return indexService.getInitBySize(list.size());
        }
        List<String> ids = list.stream().map(eventData -> getPkidValue(index, eventData)).collect(Collectors.toList());
        return indexService.batchDeleteByIds(index, type, ids, indexConfigServiceFactory.getDynamicIndexdMap(index, type, ids));
    }

    private String getPkidValue(String index, EventData eventData) {
        String pkidValue = getPkidValue(index, eventData, indexConfigServiceFactory.getPkidFormateMap(index));
        if (null == pkidValue) {
            pkidValue = NO_FIELD_KEY;
            LogUtils.log(ERROR, logger, () -> "=getPkidValue=>index:%s ,table:%s ,data:%s", index, eventData.getTableName(), eventData.toString());
        }
        return pkidValue;
    }

    private String getPkidValue(String index, EventData eventData, String pkidName) {
        String key = StringUtils.isEmpty(pkidName) ? eventData.getKeys().get(0).getColumnValue() : eventData.getKeys().stream()
                .filter(eventColumn -> pkidName.equals(indexConfigServiceFactory.getFieldFormateMap(index, eventData.getTableName(), eventColumn.getColumnName())))
                .findFirst().orElse(eventData.getColumns().stream()
                        .filter(eventColumn -> pkidName.equals(indexConfigServiceFactory.getFieldFormateMap(index, eventData.getTableName(), eventColumn.getColumnName())))
                        .findFirst().orElse(EventColumn.builder().columnName(index + "_no_pkid").build())).getColumnValue();
        if (null == key) {
            key = NO_FIELD_KEY;
            LogUtils.log(ERROR, logger, () -> "=getPkidValue=>index%s ,table:%s ,pkidName:%s ,data:%s", index, eventData.getTableName(), pkidName, eventData.toString());
        }
        return key;
    }

    public int[] batchSingleSaveByIds(String index, String type, List<EventData> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(list.get(0).getKeys())) {
            logger.warn("=batchSingleSaveByIds=>schemaName=" + list.get(0).getSchemaName() + ",tableName=" + list.get(0).getTableName() + " no pkid.");
            return indexService.getInitBySize(list.size());
        }
        Map<String, Map<String, Object>> idDatas = list.stream()
                .collect(Collectors
                        .toMap(eventData -> getPkidValue(index, eventData),
                                eventData -> parseColumnsToMap(index, list.get(0).getTableName(), new ArrayList<EventColumn>() {{
                                    addAll(eventData.getKeys());
                                    addAll(eventData.getColumns());
                                }}, OperateType.INSERT)
                                , (t1, t2) -> t1));
        return indexService.batchSaveByIds(index, type, idDatas, indexConfigServiceFactory.getDynamicIndexdMap(index, type, OperateType.INSERT, idDatas));
    }

    /**
     * FIXME slavePkidName               RealTableFkIdName
     * 1    curriculum	26	curriculum_id	17	clazz_id	                curriculum_classId	        26	25		2018-07-11 14:32:00	2018-07-11 20:53:32
     * 2	curriculum	26	curriculum_id	24	classtimeType_id	        curriculum_classtimeTypeId	26	25		2018-07-11 14:35:37	2018-07-11 20:53:07
     * 4	curriculum	26	curriculum_id	22	classRegistCount_classId	curriculum_classId	        26	25		2018-07-11 14:40:06	2018-07-11 22:08:48
     * 6	curriculum	26	curriculum_id	18	classtime_id	            curriculum_classtimeId	    26	25		2018-08-03 16:27:14	2018-08-03 16:29:29
     * <p>
     * 3    curriculum	26	curriculum_id	23	classlevel_id	            clazz_levelId	            26	25		2018-07-11 14:37:55	2018-07-11 20:52:16
     * 5	curriculum	26	curriculum_id	21	department_id	            clazz_servicecenterId	    26	25		2018-07-11 14:43:22	2018-07-11 20:48:45
     * <p>
     * <p>
     * 7	regist_stage 52	rgse_id	       51	stu_id	                    rgse_studentId	            0	46	    2018-12-19 18:00:08	2018-12-20 14:26:28		stu_id
     * 8	regist_stage 52	rgse_id	       50	rg_id	                    rgse_registId	            0	46		2018-12-20 14:28:23	2018-12-20 14:28:23		rg_id
     * 9	regist_stage 52	rgse_id	       2	clazz_id	                rgse_classId	            0	46		2018-12-20 14:33:32	2018-12-20 14:33:32		clazz_id
     * 10	regist_stage 52	rgse_id	       57	clase_id	                rgse_classStageId	        0	46		2018-12-20 14:54:12	2018-12-20 14:54:12		clase_id
     * <p>
     * <p>
     * insert into `retl`.`retl_buffer` ( `GMT_CREATE`, `PK_DATA`, `TABLE_ID`, `TYPE`, `FULL_NAME`, `GMT_MODIFIED`) values
     * ( '2018-08-08 17:10:23', '0000000049571bfa01495a55106e02b7', '0', 'I', 'otter.tb_class', '2018-08-08 17:10:23');
     */
    public int[] batchWideSaveByIds(String index, String type, List<EventData> list, List<WideTable> allWideTables, Pipeline pipeline) {
        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(list.get(0).getKeys()) || CollectionUtils.isEmpty(allWideTables)) {
            logger.warn("=batchSaveByIds=>schemaName=" + list.get(0).getSchemaName() + ",tableName=" + list.get(0).getTableName() + " no pkid.");
            return indexService.getInitBySize(list.size());
        }
        Map<String, Map<String, Object>> idDatas =
                list.stream()
                        .collect(Collectors
                                .toConcurrentMap(eventData -> getPkidValue(index, eventData),
                                        eventData -> parseColumnsToCurrentMap(index, list.get(0).getTableName(), new ArrayList<EventColumn>() {{
                                            addAll(eventData.getKeys());
                                            addAll(eventData.getColumns());
                                        }}, OperateType.INSERT), (t1, t2) -> t1));

        //根据id进行排序，从小到大；被依赖的放在前面；
        Collections.sort(allWideTables);
        List<WideTable> tables = allWideTables.stream().filter(wideTable -> wideTable.getMainTableFkIdName()
                .startsWith(NameFormatUtils.formatName(wideTable.getMainTable().getName())))
                .collect(Collectors.toList());
        assembleWideTableColumns(index, type, tables, pipeline, idDatas);

        //被依赖的数据并发，可能会获取不到依赖的数据；
        tables = allWideTables.stream().filter(wideTable -> !wideTable.getMainTableFkIdName()
                .startsWith(NameFormatUtils.formatName(wideTable.getMainTable().getName())))
                .collect(Collectors.toList());
        assembleWideTableColumns(index, type, tables, pipeline, idDatas);
        handleWidetableColumns(index, idDatas, OperateType.INSERT);
        return indexService.batchSaveByIds(index, type, idDatas, indexConfigServiceFactory.getDynamicIndexdMap(index, type, OperateType.INSERT, idDatas));
    }

    private void assembleWideTableColumns(String index, String type, List<WideTable> allWideTables, Pipeline pipeline, Map<String, Map<String, Object>> wideIdDatas) {
        List<CompletableFuture<String>> futures = new ArrayList<>(allWideTables.size());
        allWideTables.forEach(wideTable -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                LoadRoute route = getLoadRouteOfSlaveTable(pipeline, wideTable);
                if (null != route && route.getType().isLoadSingleIndex()) {
                    DataMedia<? extends DataMediaSource> slaveTableIndex = route.getLoadDataMedia();
                    List<Object> ids = wideIdDatas.values().stream().map(value -> value.get(wideTable.getMainTableFkIdName())).filter(id -> null != id).collect(Collectors.toList());
                    BoolQueryBuilder bool = QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(wideTable.getSlaveTableFkIdName(), ids));
                    Map<String, Map<String, Object>> slaveIdMap = indexService.getDataMapByIds(slaveTableIndex.getNamespace(), slaveTableIndex.getName(), bool, new ArrayList<>(0)
                            , Lists.newArrayList(IndexConfigServiceFactory.ES_DATE, IndexConfigServiceFactory.ES_DATE_TIME), wideTable.getSlaveTableFkIdName());

                    //增加与主表直接关联的表的，部分字段；
                    if (MapUtils.isNotEmpty(slaveIdMap)) {
                        wideIdDatas.forEach((s, valueMap) -> {
                            Object slaveId = valueMap.get(wideTable.getMainTableFkIdName());
                            if (null != slaveId) {
                                Map<String, Object> m = slaveIdMap.get(slaveId);
                                if (null != m) {
                                    m.entrySet().forEach(entry -> {
                                        if (null != entry.getValue() && null != entry.getKey()/* && null == valueMap.get(entry.getKey())*/) {
                                            valueMap.put(entry.getKey(), entry.getValue());
                                        }
                                    });
                                }
                            }
                        });
                    }
                } else {
                    LogUtils.log(ERROR, logger, () -> "=assembleWideTableColumns=>error ,tableName:%s has not open channel . ", wideTable.getSlaveTable().getName());
                }
                return wideTable.getSlaveTable().getName() + " done!";
            }, service);
            futures.add(future);

        });
        if (!CollectionUtils.isEmpty(futures)) {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
            List<Throwable> exceptions = new ArrayList(1);
            if (allOf.isCompletedExceptionally()) {
                allOf.exceptionally(e -> {
                    exceptions.add(e);
                    return null;
                });
                log(ERROR, logger, () -> "=batchSaveByIds=>add master error:%s ,master index name :%s ,index type:%s ,pipeline name:%s"
                        , () -> new Object[]{exceptions.get(0), index, type, pipeline.getName()});
                if (!exceptions.isEmpty()) {
                    throw new RuntimeException(exceptions.get(0));
                }
            } else {
                allOf.join();
            }
        }
    }

    /**
     * 先获取当前pipeline的loadRoute；如果不存在可能存在与其他的pipeline里面；
     */
    private LoadRoute getLoadRouteOfSlaveTable(Pipeline pipeline, WideTable wideTable) {
        LoadRoute loadRoute = ConfigHelper.findLoadRoute(pipeline, wideTable.getSlaveTable().getId(), DataMediaType.INDEX);
        if (null == loadRoute) {
            for (Pipeline other : configClientService.getCachedPipelines()) {
                loadRoute = ConfigHelper.findLoadRoute(other, wideTable.getSlaveTable().getId(), DataMediaType.INDEX);
                if (null != loadRoute) {
                    break;
                }
            }
        }
        if (null == loadRoute) {
            //load from db ;FIXME
            loadRoute = LoadRoute.builder().type(LoadType.SINGLE_INDEX).loadDataMedia(DataMedia.builder().name("udip").namespace("clase").build()).build();
        }
        return loadRoute;
    }


    /**
     * 处理宽表插入时，相关的字段逻辑处理；
     * FIXME 组合需要的字段？该数据范围比较大；
     *
     * @param idDatas
     */
    private void handleWidetableColumns(String index, Map<String, Map<String, Object>> idDatas, OperateType type) {
        idDatas.values().stream().forEach(valeMap -> {
            valeMap.putAll(indexConfigServiceFactory.getSpecialFieldMap(index, valeMap, type));
        });
    }

    public int[] batchSingleUpdateByIds(String index, String type, List<EventData> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(list.get(0).getKeys()) || list.get(0).getKeys().size() != 1) {
            logger.warn("=batchSingleUpdateByIds=>schemaName=" + list.get(0).getSchemaName() + ",tableName=" + list.get(0).getTableName() + " no pkid.");
            return indexService.getInitBySize(list.size());
        }
        Map<String, Map<String, Object>> pkidMap = list.stream()
                .collect(Collectors
                        .toMap(eventData -> getPkidValue(index, eventData),
                                eventData -> parseColumnsToMap(index, list.get(0).getTableName(), new ArrayList<EventColumn>() {{
                                    addAll(eventData.getKeys());
                                    addAll(eventData.getColumns());
                                }}, OperateType.UPDATE), (t1, t2) -> t1));
        return indexService.batchUpdateByIds(index, type, pkidMap, indexConfigServiceFactory.getDynamicIndexdMap(index, type, OperateType.UPDATE, pkidMap));
    }

    public int[] batchWideUpdateByIds(String index, String type, List<EventData> list, List<WideTable> allWideTables, Pipeline pipeline) {
        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(list.get(0).getKeys()) || CollectionUtils.isEmpty(allWideTables)) {
            logger.warn("=batchWideUpdateByIds=>schemaName=" + list.get(0).getSchemaName() + ",tableName=" + list.get(0).getTableName() + " no pkid.");
            return indexService.getInitBySize(list.size());
        }
        ArrayList<EventData> needUpdateList = Lists.newArrayList();
        list.forEach(eventData -> {
            boolean needUpdate = eventData.getUpdatedColumns().stream()
                    .filter(column -> indexConfigServiceFactory.getWideIndexLinkSubTableField(index, NameFormatUtils.formatName(allWideTables.get(0).getMainTable().getName()))
                            .contains(column.getColumnName())).findFirst().isPresent();
            if (needUpdate) {
                needUpdateList.add(eventData);
            }
        });

        if (CollectionUtils.isEmpty(needUpdateList)) {
            log(WARN, logger, () -> "=batchWideUpdateByIds=>no field need update for index:%s ,list:%s", () -> new Object[]{index, JsonUtils.marshalToString(list)});
            return new int[0];
        }
        Map<String, Map<String, Object>> pkidMap =
                needUpdateList.stream()
                        .collect(Collectors
                                .toConcurrentMap(eventData -> getPkidValue(index, eventData),
                                        eventData -> parseColumnsToCurrentMap(index, needUpdateList.get(0).getTableName(), new ArrayList<EventColumn>() {{
                                            addAll(eventData.getKeys());
                                            addAll(eventData.getColumns());
                                        }}, OperateType.UPDATE), (t1, t2) -> t1));

        ArrayList<WideTable> subWides = assembleSubTableForFkUpdated(index, type, needUpdateList, allWideTables, pipeline, pkidMap);

        //获取级联表
        if (!CollectionUtils.isEmpty(subWides)) {
            List<String> updateSubtableNames = subWides.stream().map(WideTable::getSlaveTable).map(DataMedia::getName).map(name -> NameFormatUtils.formatName(name)).collect(Collectors.toList());
            List<WideTable> linkSubTables = allWideTables.stream().filter(wideTable -> updateSubtableNames.contains(wideTable.getMainTableFkIdName().split("_")[0])).collect(Collectors.toList());
            assembleWideTableColumns(index, type, linkSubTables, pipeline, pkidMap);
        }

        //特殊字段处理
        handleSpecialFieldForUpdated(index, needUpdateList, pkidMap, OperateType.UPDATE, pkidMapParam -> {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(allWideTables.get(0).getMainTablePkIdName(), pkidMapParam.keySet()));
            List<String> fetchFields = Lists.newArrayList(allWideTables.get(0).getMainTablePkIdName());
            fetchFields.addAll(indexConfigServiceFactory.getTableNeedField(NameFormatUtils.formatName(allWideTables.get(0).getMainTable().getName())));
            Map<String, Map<String, Object>> wideIdMaps = indexService.getDataMapByIds(index, type, queryBuilder, fetchFields, Lists.newArrayList(IndexConfigServiceFactory.ES_DATE_TIME, IndexConfigServiceFactory.ES_DATE), allWideTables.get(0).getMainTablePkIdName());
            pkidMapParam.forEach((k, v) -> {
                if (null != wideIdMaps.get(k)) {
                    wideIdMaps.get(k).forEach((vk, vv) -> {
                        v.putIfAbsent(vk, vv);
                    });
                } else {
                    LogUtils.log(ERROR, logger, () -> "=batchWideUpdateByIds=>The index:%s not the key:%s", index, k);
                }
            });
            return pkidMapParam;
        });
        return indexService.batchUpdateByIds(index, type, pkidMap, indexConfigServiceFactory.getDynamicIndexdMap(index, type, OperateType.UPDATE, pkidMap));
    }

    private ArrayList<WideTable> assembleSubTableForFkUpdated(String index, String type, List<EventData> list, List<WideTable> allWideTables, Pipeline pipeline, Map<String, Map<String, Object>> pkidMap) {
        //外键更新
        ArrayList<WideTable> subWides = Lists.newArrayList();
        list.forEach(eventData -> {
            allWideTables.forEach(wideTable -> {
                Optional<EventColumn> first = eventData.getUpdatedColumns().stream().filter(column -> wideTable.getMainTableFkIdName().equals(indexConfigServiceFactory.getFieldFormateMap(index, list.get(0).getTableName(), column.getColumnName()))).findFirst();
                if (first.isPresent() && !subWides.contains(wideTable)) {
                    subWides.add(wideTable);
                }
            });
        });
        assembleWideTableColumns(index, type, subWides, pipeline, pkidMap);
        return subWides;
    }

    //slave add or update
    public int[] batchWideUpdateBySlave(String wideIndex, String type, List<EventData> slaveList, List<WideTable> allWideTables, WideTable slaveWideTable, Pipeline pipeline, OperateType operateType) {

        //some slave table add not need update wide index .
        if (operateType.isInsert() && !indexConfigServiceFactory.slaveTableAddedIsNeedWide(wideIndex, slaveWideTable.getSlaveTable().getName())) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(slaveList)) {
            return new int[0];
        }
        if (CollectionUtils.isEmpty(slaveList.get(0).getKeys()) || slaveList.get(0).getKeys().size() != 1) {
            logger.warn("=batchWideUpdateBySlave=>schemaName=" + slaveList.get(0).getSchemaName() + ",tableName=" + slaveList.get(0).getTableName() + " no pkid.");
            return indexService.getInitBySize(slaveList.size());
        }
        ArrayList<EventData> needUpdateList = Lists.newArrayList();
        if (operateType.isUpdate()) {
            slaveList.forEach(eventData -> {
                boolean needUpdate = eventData.getUpdatedColumns().stream()
                        .filter(column -> indexConfigServiceFactory.getWideIndexLinkSubTableField(wideIndex, NameFormatUtils.formatName(slaveWideTable.getSlaveTable().getName())).contains(column.getColumnName())).findFirst().isPresent();
                if (needUpdate) {
                    needUpdateList.add(eventData);
                }
            });
        } else {
            needUpdateList.addAll(slaveList);
        }
        if (CollectionUtils.isEmpty(needUpdateList)) {
            logger.warn("=batchWideUpdateBySlave=>pipelineName:" + pipeline.getName() + " , no need update index:" + wideIndex + " ,schemaName=" + slaveList.get(0).getSchemaName() + ",tableName=" + slaveList.get(0).getTableName());
            return indexService.getInitBySize(slaveList.size());
        }

        Map<String, Map<String, Object>> slavePkidMap =
                needUpdateList.stream()
                        .collect(Collectors
                                .toConcurrentMap(eventData -> getPkidValue(wideIndex, eventData, slaveWideTable.getSlaveMainTablePkIdName() == null ? slaveWideTable.getSlaveTableFkIdName() : slaveWideTable.getSlaveMainTablePkIdName()),
                                        eventData -> parseColumnsToCurrentMap(wideIndex, needUpdateList.get(0).getTableName(), new ArrayList<EventColumn>() {{
                                            addAll(eventData.getKeys());
                                            addAll(eventData.getColumns());
                                        }}, OperateType.UPDATE), (t1, t2) -> t1));

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(slaveWideTable.getMainTableFkIdName(), slavePkidMap.keySet()));
        List<String> fetchFields = Lists.newArrayList(slaveWideTable.getMainTablePkIdName(), slaveWideTable.getMainTableFkIdName());
        List<String> needFieldList = indexConfigServiceFactory.getTableNeedField(NameFormatUtils.formatName(needUpdateList.get(0).getTableName()));
        fetchFields.removeAll(needFieldList);
        fetchFields.addAll(needFieldList);
        Map<String, Map<String, Object>> wideIdMaps = indexService.getDataMapByIds(wideIndex, type, queryBuilder, fetchFields, Lists.newArrayList(IndexConfigServiceFactory.ES_DATE, IndexConfigServiceFactory.ES_DATE_TIME), slaveWideTable.getMainTablePkIdName());
        if (MapUtils.isEmpty(wideIdMaps)) {
            LogUtils.log(WARN, logger, () -> "=batchWideUpdateBySlave=>pipelineName:%s ,can not get values from wideIndex:%s ,tableName:%s , Ids:%s", pipeline.getName(), wideIndex, slaveList.get(0).getTableName(),
                    slavePkidMap.keySet().stream().collect(Collectors.joining(",")));
            return new int[0];
        }
        wideIdMaps.forEach((k, v) -> {
            Map<String, Object> map = slavePkidMap.get(null == slaveWideTable.getSlaveMainTablePkIdName() ? v.get(slaveWideTable.getMainTableFkIdName()) : k);
            if (null != map) {
                v.putAll(map);
            }
        });
        assembleSubTableForFkUpdated(wideIndex, type, needUpdateList, allWideTables, pipeline, wideIdMaps);
        handleSpecialFieldForUpdated(wideIndex, needUpdateList, wideIdMaps, operateType, null);
        return indexService.batchUpdateByIds(wideIndex, type, wideIdMaps, indexConfigServiceFactory.getDynamicIndexdMap(wideIndex, type, OperateType.UPDATE, wideIdMaps));
    }

    private void handleSpecialFieldForUpdated(String wideIndex, List<EventData> dataList, Map<String, Map<String, Object>> wideIdMaps, OperateType type
            , Function<Map<String, Map<String, Object>>, Map<String, Map<String, Object>>> fetchFieldFun) {

        if (type.isUpdate()) {

            //特殊字段处理
            boolean needUpdate = false;
            for (EventData eventData : dataList) {
                Optional<EventColumn> first = eventData.getUpdatedColumns().stream()
                        .filter(column -> indexConfigServiceFactory.getNeedUpdateFields(wideIndex)
                                .contains(indexConfigServiceFactory.getFieldFormateMap(wideIndex, dataList.get(0).getTableName(), column.getColumnName())))
                        .findFirst();
                if (first.isPresent()) {
                    needUpdate = true;
                    break;
                }
            }
            if (needUpdate) {
                if (null != fetchFieldFun) {
                    wideIdMaps = fetchFieldFun.apply(wideIdMaps);
                }
                handleWidetableColumns(wideIndex, wideIdMaps, type);
            }
        } else {
            if (null != fetchFieldFun) {
                wideIdMaps = fetchFieldFun.apply(wideIdMaps);
            }
            handleWidetableColumns(wideIndex, wideIdMaps, type);
        }
    }


    private Map<String, Object> parseColumnsToMap(String tableName, List<EventColumn> columns, EventType type) {
        Map<String, Object> jsonMap = new HashMap<>();
        columns.forEach(column -> {
            if (column != null) {
                jsonMap.put(NameFormatUtils.formatName(tableName) + table_field_sufix + NameFormatUtils.formatName(column.getColumnName()), column.isNull() ? null
                        : indexService.getIndexObject(column.getColumnType(), column.getColumnValue()));
                FieldMapping.convertValues(NameFormatUtils.formatName(tableName) + table_field_sufix + NameFormatUtils.formatName(column.getColumnName()), column, jsonMap);
            }
        });

        //when insert need add field for wide index ;
        if (type.isInsert()) {
            FieldMapping.addColumns(NameFormatUtils.formatName(tableName), jsonMap);
        }
        jsonMap.put("esDate", DateUtils.nowStr());
        jsonMap.put("esDateTime", OffsetDateTime.now().toEpochSecond());
        return jsonMap;
    }

    private Map<String, Object> parseColumnsToMap(String index, String tableName, List<EventColumn> columns, OperateType type) {
        Map<String, Object> jsonMap = new HashMap<>();
        columns.forEach(column -> {
            if (column != null) {
                jsonMap.put(indexConfigServiceFactory.getFieldFormateMap(index, tableName, column.getColumnName()), column.isNull() ? null
                        : indexService.getIndexObject(column.getColumnType(), column.getColumnValue()));
                jsonMap.putAll(indexConfigServiceFactory.getFieldTypeMap(index, tableName, column));
            }
        });
        jsonMap.putAll(indexConfigServiceFactory.getFixFieldMap(index, type));
        return jsonMap;
    }

    private Map<String, Object> parseColumnsToCurrentMap(String index, String tableName, List<EventColumn> columns, OperateType type) {
        Map<String, Object> jsonMap = new ConcurrentHashMap<>();
        try {
            columns.forEach(column -> {
                if (column != null && !column.isNull()) {
                    jsonMap.put(indexConfigServiceFactory.getFieldFormateMap(index, tableName, column.getColumnName()), indexService.getIndexObject(column.getColumnType(), column.getColumnValue()));
                    jsonMap.putAll(indexConfigServiceFactory.getFieldTypeMap(index, tableName, column));
                }
            });
            jsonMap.putAll(indexConfigServiceFactory.getFixFieldMap(index, type));
        } catch (Exception e) {
            LogUtils.log(ERROR, logger, () -> "=parseColumnsToCurrentMap=>index:%s ,tableName:%s ,type:%s", index, tableName, type);
            throw new RuntimeException(LogUtils.format("=parseColumnsToCurrentMap=>index:%s ,tableName:%s ,type:%s ,error:%s", index, tableName, type, LogUtils.getFullStackTrace(e)));
        }
        return jsonMap;
    }

    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    @Override
    public void destroy() throws Exception {
        indexService.destroy();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Assert.assertNotNull(applicationContext, "applicationContext is null.");
        this.applicationContext = applicationContext;
    }

    public void setIndexConfigServiceFactory(IndexConfigServiceFactory indexConfigServiceFactory) {
        this.indexConfigServiceFactory = indexConfigServiceFactory;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public static void main(String[] args) {

    }
}
