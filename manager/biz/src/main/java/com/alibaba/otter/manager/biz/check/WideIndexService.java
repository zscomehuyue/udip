package com.alibaba.otter.manager.biz.check;

import com.alibaba.otter.common.push.datasource.DataSourceService;
import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.common.push.index.es.IndexJsonService;
import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.common.push.index.wide.config.WideHelper;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.config.widetable.WideTableService;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.page.PageList;
import com.alibaba.otter.shared.common.utils.DateUtils;
import com.alibaba.otter.shared.common.utils.DingtalkUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.common.utils.NameFormatUtils;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

public class WideIndexService {
    public static final int DIRY_DATA_SIZE_SMALL = 500;
    public static int SLEEP_SMALL_FOR_HANDLE_OVER = 120;
    public static final int ONE_SECONDS = 1;
    public static final int FIX_NEXT_CHECK_MINUTS = 30;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    //FIXME retl schema is fix; TODO dynamic ;
    public static final String RELT_INSERT_SQL = "INSERT INTO udip_retl.retl_buffer ( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME, GMT_MODIFIED) values " +
            "( now(), '%s', '0', 'I', '%s', now()) ";

    public static final String RELT_CONDITION_SQL = "INSERT INTO udip_retl.retl_buffer ( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME, GMT_MODIFIED) " +
            "( select now(), %s, '0', 'I', '%s', now() from %s where %s )";

    public static int SLEEP_FOR_HANDLE_OVER = 300;
    protected ScheduledExecutorService timer;
    protected ExecutorService service;
    protected IndexJsonService indexService;
    protected IndexService defaultIndexService;
    private WideTableService wideTableService;
    private PipelineService pipelineService;
    private ChannelService channelService;
    private DataSourceService dataSourceService;
    protected final static int DEPLY_RUN_CHECK = 2;
    protected String threadName = "FreeDoor";
    private volatile boolean isRun = true;
    private volatile boolean isRunUpdate = true;
    private volatile boolean scanIndexRuning = true;
    public List<Long> excludePipelines = Lists.newArrayList();

    /**
     * believe event can compensate , so timer need less ,30 seconds delay check  ;
     */
    protected final static int FIX_RUN_CHECK = 5;
    public static final int FEATCH_PAGE_SIZE = 5000;
    public static final String TABLE_SPLIT_SUFFIX = "_";
    public int fixNextSeconds = FIX_RUN_CHECK;

    public void initial() {
        service = Executors.newFixedThreadPool(100, new NamedThreadFactory("-" + threadName + "-compensate-wide-"));
        timer = Executors.newScheduledThreadPool(2, new NamedThreadFactory("-" + threadName + "-compensate-wide-timer-"));
    }

    @Deprecated
    public void handleWideIndex() {
        handleWideIndex((dataMedia, wideTables, pipeline) -> handleWideIndexForStatus(dataMedia, wideTables, pipeline));
    }

    public void handleAllPipelineForClassUpdated(String[] years, Long esDateTime, Long endTime) {
        handleWideIndex((dataMedia, wideTables, pipeline) -> handleWideIndexForUpdateKey(dataMedia, wideTables, pipeline, years, esDateTime, endTime));
    }

    public void handleAllPipelineByCucModifyDate(String[] years, String startTime, String endTime, String check, String pipelineId) {
        handleWideIndex((dataMedia, wideTables, pipeline) -> handleWideIndexByCucModifyDate(dataMedia, wideTables, pipeline, years, startTime, endTime, check, pipelineId));
    }

    protected String getFullName(String tableName, Pipeline pipeline) {
        return pipeline.getPairs().stream().filter(dataMediaPair -> {
            if (NameFormatUtils.formatName(dataMediaPair.getTarget().getName()).equals(tableName)) {
                return true;
            }
            return false;
        }).map(dataMediaPair -> (dataMediaPair.getSource().getNamespace() + "." + dataMediaPair.getSource().getName()))
                .findFirst().orElse(null);
    }

    //FIXME no data to get ;
    protected List<Pipeline> getPipelinesByTargetName(String tableName) {
        List<Pipeline> list = pipelineService.listAll().stream().filter(pipe -> {
            return pipe.getPairs().stream().filter(dataMediaPair -> {
                if (NameFormatUtils.formatName(dataMediaPair.getTarget().getName()).equals(tableName)) {
                    return true;
                }
                return false;
            }).findFirst().isPresent();
        }).collect(Collectors.toList());

        if (org.apache.commons.collections.CollectionUtils.isEmpty(list)) {
            LogUtils.log(ERROR, logger, () -> "=getPipelinesByTargetTableName=>can not get pipeline from local cache for table name=%s", tableName);
        }
        return list;
    }

    protected Channel getChannel(Pipeline pipeline) {
        return channelService.findByPipelineId(pipeline.getId());
    }


    /**
     * 查找宽表外键，及子表外键；不存在的情况；
     *
     * @param needDate
     */
    @Deprecated
    public void scanWideIndex(boolean needDate) {
        try {
            scanIndexRuning = true;
            handleWideIndex(needDate, true);
        } finally {
            scanIndexRuning = false;
        }
    }

    /**
     * 根据索引的id，来处理宽表索引的状态；
     *
     * @param wideIndexId
     */
    public void handleWideIndexStatus(Long wideIndexId) {
        handleWideIndex(() -> wideTableService.listByTargetIdAndTableId(wideIndexId, -1L), (DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline) -> {
//            List<HashMap> list = getDataList(FieldHelper.getCityCode(pipeline.getPairs().get(0)), dataMedia, Lists.newArrayList(), false);
//            defaultIndexService.getDataMapByIds();

        });
    }

    /**
     * handle wide index only and insert retl by ids ;
     */
    public void handleWideIndex(HandleWideIndexCallBack callBack) {
        LogUtils.log(INFO, logger, () -> "=handleWideIndex=>");
        List<WideTable> allWideTables = wideTableService.listByTargetIdAndTableId(-1L, -1L);
        handleWideIndex(() -> allWideTables, callBack);
    }

    public void handleWideIndex(Supplier<List<WideTable>> wideTableFun, HandleWideIndexCallBack callBack) {
        LogUtils.log(INFO, logger, () -> "=handleWideIndex=>");
        List<WideTable> allWideTables = wideTableFun.get();
        if (CollectionUtils.isEmpty(allWideTables)) {
            return;
        }
        ConcurrentMap<DataMedia, List<WideTable>> indexMap = new MapMaker().makeMap();
        Collections.sort(allWideTables);
        allWideTables.forEach(wideTable -> {
            indexMap.putIfAbsent(wideTable.getTarget(), new ArrayList<>());
            indexMap.get(wideTable.getTarget()).add(wideTable);
        });
        ArrayList<CompletableFuture> list = Lists.newArrayList();
        indexMap.forEach((dataMedia, wideTables) -> {
            List<Pipeline> pipelines = getPipelinesByTargetName(wideTables.get(0).getSlaveTableFkIdName().split("_")[0]);
            if (!CollectionUtils.isEmpty(pipelines)) {
                pipelines.forEach(pipeline -> {
                    CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                        if (ConfigHelper.needLoadMedia(pipeline, DataMediaType.INDEX)
                                && getChannel(pipeline).getStatus().isStart()
                                && !pipeline.getParameters().getSkipFreedom()) {
                            callBack.handleWideIndex(dataMedia, wideTables, pipeline);
                        } else {
                            LogUtils.log(INFO, logger, () -> "=handleWideIndex=>channel:%s is not start or the channel has no the index %s .", getChannel(pipeline).getName(), dataMedia.getNamespace());
                        }
                        return null;
                    }, service);
                    list.add(future);
                });
            }
        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).join();
    }

    /**
     * 查询状态为0的数据进行retl
     *
     * @param dataMedia
     * @param wideTables
     * @param pipeline
     */
    private void handleWideIndexForStatus(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline) {
        List<HashMap> dataList = getDataList("", dataMedia, Lists.newArrayList(wideTables.get(0).getMainTablePkIdName()), false);
        Optional.ofNullable(dataList).ifPresent(datas -> {
            List<String> ids = datas.stream().map(map -> map.get(wideTables.get(0).getMainTablePkIdName()).toString()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ids)) {
                DingtalkUtils.sendMsg("udip-manager", LogUtils.format("=handleWideIndexForStatus=>Pipeline name %s have dirty data need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size()));
                List<String> sqls = getSqls(ids, pipeline, wideTables.get(0), true);
                batchRetl(pipeline, wideTables.get(0), sqls);
            }
        });
    }

    /**
     * spend more time ?
     *
     * @param dataMedia
     * @param wideTables
     * @param pipeline
     */
    public void handleWideIndexForUpdateKey(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, String[] years, Long esDateTime, Long esEndDateTime) {
        LogUtils.log(INFO, logger, () -> "=handleWideIndexForUpdateKey=>check index:%s ,pipeline name:%s ", dataMedia.getNamespace(), pipeline.getName());
        if (excludePipelines.contains(pipeline.getId())) {
            LogUtils.log(WARN, logger, () -> "=handleWideIndexForUpdateKey=>exclude check index:%s ,pipeline name:%s ", dataMedia.getNamespace(), pipeline.getName());
            return;
        }
        long start = System.currentTimeMillis();
        String cityId = FieldHelper.getCityCode(pipeline.getPairs().get(0));
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (null != years) {
            builder.filter(QueryBuilders.termsQuery("clazz_year", Arrays.asList(years)));
        }
        builder.filter(QueryBuilders.termQuery("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED));
        if (!StringUtils.isEmpty(cityId)) {
            builder.filter(QueryBuilders.termQuery(NameFormatUtils.formatName(dataMedia.getNamespace()) + "_cityId", cityId));
        }
        if (null != esDateTime && null != esEndDateTime) {
            builder.filter(QueryBuilders.rangeQuery("esDateTime").gte(esDateTime).lte(esEndDateTime));
        } else {
            if (null != esDateTime) {
                builder.filter(QueryBuilders.rangeQuery("esDateTime").gte(esDateTime));
            }
            if (null != esEndDateTime) {
                builder.filter(QueryBuilders.rangeQuery("esDateTime").lte(esEndDateTime));
            }
        }
        int pageSize = 1000;
        ArrayList<String> includeFields = Lists.newArrayList(wideTables.get(0).getMainTablePkIdName(), "clazz_maxPersons", "remainCount"
                , "classRegistCount_registCount", "curriculum_changeoutCourseNum", "curriculum_changeinCourseNum");
        wideTables.forEach(wideTable -> {
            includeFields.add(wideTable.getSlaveTableFkIdName());
            includeFields.add(wideTable.getMainTableFkIdName());
        });
        PageList<Map<String, Object>> page = handleAndRetl(dataMedia, wideTables, pipeline, builder, includeFields, 0, pageSize);
        for (int i = 1; i <= page.getTotalPage(); i++) {
            LogUtils.log(INFO, logger, () -> "=handleWideIndexForUpdateKey=>check index:%s , pipeline name:%s ,page:%s ,spend:%s ", dataMedia.getNamespace(), pipeline.getName(), String.valueOf(i), (System.currentTimeMillis() - start));
            handleAndRetl(dataMedia, wideTables, pipeline, builder, includeFields, i, pageSize);
        }
        LogUtils.log(INFO, logger, () -> "=handleWideIndexForUpdateKey=>check index:%s , pipeline name:%s ,record:%s ,spend:%s ", dataMedia.getNamespace(), pipeline.getName(), page.getTotal(), (System.currentTimeMillis() - start));
    }

    public void handleWideIndexByCucModifyDate(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, String[] years, String startTime, String endTime, String check, String pipelineId) {
        if (!StringUtils.isEmpty(pipelineId) && !pipelineId.equalsIgnoreCase(pipeline.getId().toString())) {
            LogUtils.log(WARN, logger, () -> "=handleWideIndexByCucModifyDate=>exclude check index:%s ,pipeline name:%s ", dataMedia.getNamespace(), pipeline.getName());
            return;
        }
        LogUtils.log(INFO, logger, () -> "=handleWideIndexByCucModifyDate=>check index:%s ,pipeline name:%s ", dataMedia.getNamespace(), pipeline.getName());
        if (excludePipelines.contains(pipeline.getId())) {
            LogUtils.log(WARN, logger, () -> "=handleWideIndexByCucModifyDate=>exclude check index:%s ,pipeline name:%s ", dataMedia.getNamespace(), pipeline.getName());
            return;
        }
        long start = System.currentTimeMillis();
        String cityId = FieldHelper.getCityCode(pipeline.getPairs().get(0));
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (null != years) {
            builder.filter(QueryBuilders.termsQuery("clazz_year", Arrays.asList(years)));
        }
        if (!StringUtils.isEmpty(cityId)) {
            builder.filter(QueryBuilders.termQuery(NameFormatUtils.formatName(dataMedia.getNamespace()) + "_cityId", cityId));
        }
        if (null != startTime && null != endTime) {
            builder.filter(QueryBuilders.rangeQuery("curriculum_modifyTime").gte(startTime).lte(endTime));
        }
        int pageSize = 1000;
        ArrayList<String> includeFields = Lists.newArrayList(wideTables.get(0).getMainTablePkIdName(), "clazz_maxPersons", "remainCount"
                , "classRegistCount_registCount", "curriculum_changeoutCourseNum", "curriculum_changeinCourseNum");
        wideTables.forEach(wideTable -> {
            includeFields.add(wideTable.getSlaveTableFkIdName());
            includeFields.add(wideTable.getMainTableFkIdName());
        });
        LogUtils.log(INFO, logger, () -> "=handleWideIndexByCucModifyDate=>sql:  %s", builder.toString());
        PageList<Map<String, Object>> page = checkAndRetl(dataMedia, wideTables, pipeline, builder, includeFields, 0, pageSize, check);
        for (int i = 1; i <= page.getTotalPage(); i++) {
            LogUtils.log(INFO, logger, () -> "=handleWideIndexByCucModifyDate=>check index:%s , pipeline name:%s ,page:%s ,totalPage:%s ,spend:%s ", dataMedia.getNamespace(), pipeline.getName(), String.valueOf(i), page.getTotalPage(), (System.currentTimeMillis() - start));
            checkAndRetl(dataMedia, wideTables, pipeline, builder, includeFields, i, pageSize, check);
        }
        LogUtils.log(INFO, logger, () -> "=handleWideIndexByCucModifyDate=>check index:%s , pipeline name:%s ,record:%s ,spend:%s ", dataMedia.getNamespace(), pipeline.getName(), page.getTotal(), (System.currentTimeMillis() - start));
    }

    public String handleWideIndex(long pipelineId, String[] years, Long esDateTime, Long esEndDateTime) {
        LogUtils.log(INFO, logger, () -> "=handleWideIndex=>check pipelineId:%s", pipelineId);
        Pipeline pipeline = pipelineService.findById(pipelineId);
        List<WideTable> allWideTables = wideTableService.listByTargetIdAndTableId(-1L, -1L);
        if (CollectionUtils.isEmpty(allWideTables)) {
            return "no wide index .";
        }
        ConcurrentMap<DataMedia, List<WideTable>> indexMap = new MapMaker().makeMap();
        Collections.sort(allWideTables);
        allWideTables.forEach(wideTable -> {
            if (null == indexMap.get(wideTable.getTarget())) {
                indexMap.put(wideTable.getTarget(), new ArrayList<>());
            }
            indexMap.get(wideTable.getTarget()).add(wideTable);
        });
        if (null == pipeline) {
            return "no data for pipelineId=" + pipelineId;
        }
        indexMap.forEach((dataMedia, wideTables) -> {
            handleWideIndexForUpdateKey(dataMedia, wideTables, pipeline, years, esDateTime, esEndDateTime);
        });
        return "ok";
    }

    private PageList<Map<String, Object>> checkAndRetl(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, BoolQueryBuilder builder, ArrayList<String> includeFields, int pageNo, int pageSize, String check) {
        PageList<Map<String, Object>> page = defaultIndexService.getDataByPage(dataMedia.getNamespace(), dataMedia.getName(), builder
                , null, includeFields, pageNo, pageSize);
        Optional.ofNullable(page.getList()).ifPresent(datas -> {
            if (StringUtils.isEmpty(check)) {
                List<String> ids = datas.stream().map(map -> map.get(wideTables.get(0).getMainTablePkIdName()).toString()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)) {
                    DingtalkUtils.sendMsg("udip-manager-reAdd ", LogUtils.format("=checkAndRetl=>Pipeline name %s have dirty data (update class) need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size()));
                    LogUtils.log(INFO, logger, () -> "=checkAndRetl=>udip-manager-reAdd Pipeline name %s have dirty data (update class) need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size());
                    List<String> sqls = getSqls(ids, pipeline, wideTables.get(0), true);
                    batchRetl(pipeline, wideTables.get(0), sqls);
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (Exception e) {
                    }
                }
            } else {
                LogUtils.log(INFO, logger, () -> "=checkAndRetl=>check pipeline.getName:%s", pipeline.getName());
                List<String> ids = datas.stream().filter(map -> needUpdate(map, wideTables)).map(map -> map.get(wideTables.get(0).getMainTablePkIdName()).toString()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(ids)) {
                    DingtalkUtils.sendMsg("udip-manager-remainCount", LogUtils.format("=checkAndRetl=>check Pipeline name %s have dirty data (update class) need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size()));
                    LogUtils.log(INFO, logger, () -> "=checkAndRetl=>udip-manager-remainCount  Pipeline name %s have dirty data (remainCount) need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size());
                    List<String> sqls = getSqls(ids, pipeline, wideTables.get(0), true);
                    batchRetl(pipeline, wideTables.get(0), sqls);
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (Exception e) {
                    }
                }

            }
        });
        return page;
    }

    private PageList<Map<String, Object>> handleAndRetl(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, BoolQueryBuilder builder, ArrayList<String> includeFields, int pageNo, int pageSize) {
        PageList<Map<String, Object>> page = defaultIndexService.getDataByPage(dataMedia.getNamespace(), dataMedia.getName(), builder
                , null, includeFields, pageNo, pageSize);
        List<Map<String, Object>> dataList = page.getList().stream().filter(map -> needUpdate(map, wideTables)).collect(Collectors.toList());
        Optional.ofNullable(dataList).ifPresent(datas -> {
            List<String> ids = datas.stream().map(map -> map.get(wideTables.get(0).getMainTablePkIdName()).toString()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ids)) {
                DingtalkUtils.sendMsg("udip-manager-update", LogUtils.format("=handleAndRetl=>Pipeline name %s have dirty data (update class) need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size()));
                LogUtils.log(INFO, logger, () -> "=handleAndRetl=>Pipeline name %s have dirty data (update class) need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), dataMedia.getNamespace(), ids.size());
                List<String> sqls = getSqls(ids, pipeline, wideTables.get(0), true);
                batchRetl(pipeline, wideTables.get(0), sqls);
            }
        });
        return page;
    }

    public boolean needUpdate(Map<String, Object> map, List<WideTable> wideTables) {
        Optional<WideTable> first = wideTables.stream().filter(wideTable -> {
            if (null == map.get(wideTable.getMainTableFkIdName()) || null == map.get(wideTable.getSlaveTableFkIdName())) {
                LogUtils.log(INFO, logger, () -> "=needUpdate=>field is null id:%s ", map.get("curriculum_id"));
                return true;
            }
            if (!map.get(wideTable.getMainTableFkIdName()).equals(map.get(wideTable.getSlaveTableFkIdName()))) {
                LogUtils.log(INFO, logger, () -> "=needUpdate=>fkid is not same id:%s ", map.get("curriculum_id"));
                return true;
            }
            return false;
        }).findFirst();
        if (first.isPresent()) {
            return true;
        }
        try {
            int value = Integer.parseInt(map.get("clazz_maxPersons").toString())
                    - Integer.parseInt(map.get("classRegistCount_registCount") == null ? "0" : map.get("classRegistCount_registCount").toString())
                    + Integer.parseInt(map.get("curriculum_changeoutCourseNum") == null ? "0" : map.get("curriculum_changeoutCourseNum").toString())
                    - Integer.parseInt(map.get("curriculum_changeinCourseNum") == null ? "0" : map.get("curriculum_changeinCourseNum").toString());
            if (value != Integer.parseInt(map.get("remainCount").toString())) {
                LogUtils.log(INFO, logger, () -> "=needUpdate=>count:%s ,remainCount:%s  , id:%s ", value, Integer.parseInt(map.get("remainCount").toString()), map.get("curriculum_id"));
                return true;
            }
        } catch (Exception e) {
            LogUtils.log(INFO, logger, () -> "=needUpdate=>error:%s ", e);
        }
        return false;
    }

    public interface HandleWideIndexCallBack {
        void handleWideIndex(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline);
    }

    /**
     * 常用方法,更新es中的相关的数据；
     */
    //entrylist =key=value::key=value
    public String queryAndUpdateStatus(String index, String type, String pkid, int pageNo, int maxPage, int pageSize, List<String> entryList) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_INIT));
        if (!CollectionUtils.isEmpty(entryList)) {
            entryList.forEach(entry -> {
                String[] values = entry.split("=");
                builder.filter(QueryBuilders.termQuery(values[0], values[1]));
            });
        }
        if (pageSize <= 0) {
            pageSize = 1000;
        }

        LogUtils.log(WARN, logger, () -> "=queryAndUpdateStatus=>sql:%s", builder.toString());
        PageList<Map<String, Object>> page = defaultIndexService.getDataByPage(index, type, builder, Lists.newArrayList(pkid), pageNo, pageSize);
        Map<String, Map<String, Object>> idDataMap = page.getList().stream().collect(Collectors.toMap(map -> (map.get(pkid) == null ? "" : map.get(pkid).toString()), map -> {
            HashMap<String, Object> value = Maps.newHashMap();
            value.put("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);
            return value;
        }));
        defaultIndexService.batchUpdateByIds(index, type, idDataMap);
        int totalPage = page.getTotalPage();
        for (int i = pageNo + 1; i <= maxPage && i <= totalPage; i++) {
            page = defaultIndexService.getDataByPage(index, type, builder, Lists.newArrayList(pkid), 0, pageSize);
            idDataMap = page.getList().stream().collect(Collectors.toMap(map -> (map.get(pkid) == null ? "" : map.get(pkid).toString()), map -> {
                HashMap<String, Object> value = Maps.newHashMap();
                value.put("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);
                return value;
            }));
            defaultIndexService.batchUpdateByIds(index, type, idDataMap, true);
        }
        return builder.toString();
    }

    //entrylist =key=value::key=value
    public String queryAndUpdate(String index, String type, String pkid, int pageNo, int maxPage, int pageSize, List<String> conditonList, List<String> valueList) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (!CollectionUtils.isEmpty(conditonList)) {
            conditonList.forEach(entry -> {
                String[] values = entry.split("=");
                builder.filter(QueryBuilders.termQuery(values[0], values[1]));
            });
        }
        if (pageSize <= 0) {
            pageSize = 1000;
        }
        LogUtils.log(WARN, logger, () -> "=queryAndUpdate=>sql:%s", builder.toString());
        PageList<Map<String, Object>> page = defaultIndexService.getDataByPage(index, type, builder, Lists.newArrayList(pkid), pageNo, pageSize);
        Map<String, Map<String, Object>> idDataMap = page.getList().stream().collect(Collectors.toMap(map -> (map.get(pkid) == null ? "" : map.get(pkid).toString()), map -> {
            HashMap<String, Object> value = Maps.newHashMap();
            valueList.forEach(entry -> {
                String[] values = entry.split("=");
                value.put(values[0], values[1]);

            });
            return value;
        }));
        defaultIndexService.batchUpdateByIds(index, type, idDataMap);
        int totalPage = page.getTotalPage();
        for (int i = pageNo + 1; i <= maxPage && i <= totalPage; i++) {
            page = defaultIndexService.getDataByPage(index, type, builder, Lists.newArrayList(pkid), 0, pageSize);
            idDataMap = page.getList().stream().collect(Collectors.toMap(map -> (map.get(pkid) == null ? "" : map.get(pkid).toString()), map -> {
                HashMap<String, Object> value = Maps.newHashMap();
                valueList.forEach(entry -> {
                    String[] values = entry.split("=");
                    value.put(values[0], values[1]);

                });
                return value;
            }));
            defaultIndexService.batchUpdateByIds(index, type, idDataMap, true);
        }
        return builder.toString();
    }


    public void handleWideIndex(boolean needDate, boolean needCity) {
        LogUtils.log(INFO, logger, () -> "=scanWideIndex=>start");
        List<WideTable> allWideTables = wideTableService.listByTargetIdAndTableId(-1L, -1L);
        if (CollectionUtils.isEmpty(allWideTables)) {
            return;
        }
        ConcurrentMap<DataMedia, List<WideTable>> indexMap = new MapMaker().makeMap();
        Collections.sort(allWideTables);
        allWideTables.forEach(wideTable -> {
            if (null == indexMap.get(wideTable.getTarget())) {
                indexMap.put(wideTable.getTarget(), new ArrayList<>());
            }
            indexMap.get(wideTable.getTarget()).add(wideTable);
        });
        ArrayList<CompletableFuture> list = Lists.newArrayList();
        indexMap.forEach((dataMedia, wideTables) -> {
            List<Pipeline> pipelines = getPipelinesByTargetName(wideTables.get(0).getSlaveTableFkIdName().split("_")[0]);
            if (!CollectionUtils.isEmpty(pipelines)) {
                pipelines.forEach(pipeline -> {
                    if (excludePipelines.contains(pipeline.getId())) {
                        DingtalkUtils.sendMsg("udip-manager", LogUtils.format("=handleWideIndex=>Pipeline name %s is importing data ,So exclude it for auto modify data.", pipeline.getName()));
                    } else {
                        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                            if (ConfigHelper.needLoadMedia(pipeline, DataMediaType.INDEX)
                                    && getChannel(pipeline).getStatus().isStart()
                                    && !pipeline.getParameters().getSkipFreedom()) {
                                String cityId = "";
                                if (needCity) {
                                    cityId = FieldHelper.getCityCode(pipeline.getPairs().get(0));
                                }
                                compensateSlave(needDate, dataMedia, wideTables, pipeline, cityId);
                                compensateMain(needDate, dataMedia, wideTables, pipeline, cityId);
                            } else {
                                LogUtils.log(INFO, logger, () -> "=handleWideIndex=>channel:%s is not start or the channel has no the index %s .", getChannel(pipeline).getName(), dataMedia.getNamespace());
                            }
                            return null;
                        }, service);
                        list.add(future);
                    }
                });
            }
        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).join();

    }

    private void compensateSlave(boolean needDate, DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, String cityId) {
        LogUtils.log(INFO, logger, () -> "=compensateSlave=>needDate:%s ,pipele name:%s ,cityId:%s ,index:%s ,type:%s", needDate, pipeline.getName(), cityId, dataMedia.getNamespace(), dataMedia.getNamespace());
        try {
            List<String> relFields = wideTables.stream().map(WideTable::getMainTableFkIdName).collect(Collectors.toList());
            List<String> slaveIdFields = wideTables.stream().map(WideTable::getSlaveTableFkIdName).collect(Collectors.toList());
            List<String> ids = new ArrayList<>(slaveIdFields.size());
            slaveIdFields.forEach(id -> {
                String[] values = id.split(TABLE_SPLIT_SUFFIX);
                List<WideHelper.TableColumn> tableColumns = WideHelper.CURRICULUM_CONDTION_PKID_COMPENSATE.get(values[0]);
                if (!CollectionUtils.isEmpty(tableColumns)) {
                    for (WideHelper.TableColumn tableColumn : tableColumns) {
                        if (tableColumn.isPrimaryKey()) {
                            ids.add(values[0] + TABLE_SPLIT_SUFFIX + tableColumn.getTargetName());
                        }
                    }
                } else {
                    ids.add(id);
                }
            });
            relFields.addAll(ids);
            List<HashMap> dataList = getDataList(cityId, dataMedia, relFields, needDate);
            LogUtils.log(INFO, logger, () -> "=scanWideIndex=>needDate %s ,The index %s have %s rows need to modify.", needDate, dataMedia.getNamespace(), dataList.size());
            doCompensate(pipeline, dataList, wideTables, ids, false);
        } catch (Exception e) {
            LogUtils.log(ERROR, logger, () -> "=scanWideIndex=>needDate %s ,index=%s , error:%s", needDate, dataMedia.getNamespace(), e);
        }
    }

    private void compensateMain(boolean needDate, DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, String cityId) {
        LogUtils.log(INFO, logger, () -> "=compensateMain=>needDate:%s ,pipele name:%s ,cityId:%s ,index:%s", needDate, pipeline.getName(), cityId, dataMedia.getNamespace());
        try {
            List<HashMap> dataList = getDataList(cityId, dataMedia, Lists.newArrayList(wideTables.get(0).getMainTablePkIdName()), needDate);
            LogUtils.log(INFO, logger, () -> "=compensateMain=>needDate:%s ,pipele name:%s , The index %s have %s rows need to modify.", needDate, pipeline.getName(), dataMedia.getNamespace(), dataList.size());
            if (dataList.size() > 0) {
                DingtalkUtils.sendMsg("udip-manager", LogUtils.format("=compensateMain=>Pipeline name %s have dirty data need handle , msg: needDate:%s , the index %s have %s rows need to modify.", pipeline.getName(), needDate, dataMedia.getNamespace(), dataList.size()));
            }
            doCompensate(pipeline, dataList, Lists.newArrayList(wideTables.get(0)), Lists.newArrayList(wideTables.get(0).getMainTablePkIdName()), true);
            if (dataList.size() > DIRY_DATA_SIZE_SMALL) {
                TimeUnit.SECONDS.sleep(SLEEP_FOR_HANDLE_OVER);
            } else if (dataList.size() < DIRY_DATA_SIZE_SMALL && dataList.size() > 0) {
                TimeUnit.SECONDS.sleep(SLEEP_SMALL_FOR_HANDLE_OVER);
            }
        } catch (Exception e) {
            LogUtils.log(ERROR, logger, () -> "=compensateMain=>needDate:%s ,pipele name:%s ,cityId:%s ,index:%s ,errors:%s ", needDate, pipeline.getName(), cityId, dataMedia.getNamespace(), e);
        }
    }

    private List<HashMap> getDataList(String cityId, DataMedia dataMedia, List<String> includeFields, boolean needDate) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_INIT));
        if (!StringUtils.isEmpty(cityId)) {
            builder.filter(QueryBuilders.termQuery(NameFormatUtils.formatName(dataMedia.getNamespace()) + "_cityId", cityId));
        }
        LogUtils.log(INFO, logger, () -> "=getDataList=>needDate:%s , repair index:%s ,esStatus:%s ", needDate, dataMedia.getNamespace(), FieldHelper.ES_SYNC_WIDE_INDEX_INIT);
        return indexService.getDataList(dataMedia.getNamespace(), dataMedia.getName(), FEATCH_PAGE_SIZE, builder, null
                , HashMap.class, includeFields.toArray(new String[includeFields.size()]));
    }

    private String getWideSlavePkid(String realSlavePKId) {
        String tableName = realSlavePKId.split(TABLE_SPLIT_SUFFIX)[0];
        List<WideHelper.TableColumn> tableColumns = WideHelper.CURRICULUM_CONDTION_PKID_COMPENSATE.get(tableName);
        String targetName = tableColumns.stream().filter(WideHelper.TableColumn::isNotPrimaryKey).map(WideHelper.TableColumn::getTargetName).findFirst().orElse(null);
        return tableName + TABLE_SPLIT_SUFFIX + targetName;
    }

    /**
     * 1    curriculum	26	curriculum_id	17	clazz_id	                curriculum_classId	        26	25		2018-07-11 14:32:00	2018-07-11 20:53:32
     * 2	curriculum	26	curriculum_id	24	classtimeType_id	        curriculum_classtimeTypeId	26	25		2018-07-11 14:35:37	2018-07-11 20:53:07
     * 3	curriculum	26	curriculum_id	23	classlevel_id	            clazz_levelId	            26	25		2018-07-11 14:37:55	2018-07-11 20:52:16
     * 4	curriculum	26	curriculum_id	22	classRegistCount_classId	curriculum_classId	        26	25		2018-07-11 14:40:06	2018-07-11 22:08:48
     * 5	curriculum	26	curriculum_id	21	department_id	            clazz_servicecenterId	    26	25		2018-07-11 14:43:22	2018-07-11 20:48:45
     * 6	curriculum	26	curriculum_id	18	classtime_id	            curriculum_classtimeId	    26	25		2018-08-03 16:27:14	2018-08-03 16:29:29
     * <p>
     * insert into `retl`.`retl_buffer` ( `GMT_CREATE`, `PK_DATA`, `TABLE_ID`, `TYPE`, `FULL_NAME`, `GMT_MODIFIED`) values
     * ( '2018-08-08 17:10:23', '0000000049571bfa01495a55106e02b7', '0', 'I', 'otter.tb_class', '2018-08-08 17:10:23');
     */
    private void doCompensate(Pipeline pipeline, List<HashMap> dataList, List<WideTable> wideTables, List<String> idFields, boolean master) {
        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }
        Map<String, WideTable> idWideTables = wideTables.stream().collect(Collectors.toMap(master ? WideTable::getMainTablePkIdName : WideTable::getSlaveTableFkIdName, wideTable -> {
            return wideTable;
        }, (k, v) -> v));
        Map<WideTable, List<String>> needCompensate = new MapMaker().makeMap();
        idFields.forEach(id -> {
            if (null != idWideTables.get(id)) {
                findCompensateWideTables(dataList, idWideTables, needCompensate, id, master);
            } else {
                findCompensateWideTables(dataList, idWideTables, needCompensate, getWideSlavePkid(id), master);
            }

        });
        needCompensate.forEach((wideTable, list) -> {
            List<String> sqls;
            if (null != WideHelper.CURRICULUM_CONDTION_PKID_COMPENSATE.get(wideTable.getSlaveTableFkIdName().split("_")[0]) &&
                    WideHelper.CURRICULUM_CONDTION_PKID_COMPENSATE.get(wideTable.getSlaveTableFkIdName().split("_")[0]).stream()
                            .filter(tableColumn -> wideTable.getSlaveTableFkIdName().endsWith(tableColumn.getTargetName())).findFirst().isPresent()) {
                sqls = getSqlsByCondition(list, pipeline, wideTable, master);
                batchRetl(pipeline, wideTable, sqls);
            } else {
                sqls = getSqls(list, pipeline, wideTable, master);
                batchRetl(pipeline, wideTable, sqls);
            }
        });

    }


    private void findCompensateWideTables(List<HashMap> dataList, Map<String, WideTable> idWideTables, Map<WideTable, List<String>> needCompensate, String id, boolean master) {
        dataList.forEach(map -> {
            Object value = map.get(master ? idWideTables.get(id).getMainTablePkIdName() : idWideTables.get(id).getMainTableFkIdName());

            if ((null == map.get(id) || master) && null != value) {
                if (null == needCompensate.get(idWideTables.get(id))) {
                    needCompensate.put(idWideTables.get(id), new ArrayList<String>());
                }
                needCompensate.get(idWideTables.get(id)).add(value.toString());
            }
        });
    }


    //FIXME
    private void batchRetl(Pipeline pipeline, WideTable wideTable, List<String> sqls) {
        DataMedia dataMedia = ConfigHelper.findSourceDataMedia(pipeline, wideTable.getSlaveTable().getId());
        JdbcTemplate dataSource = dataSourceService.getJdbcTemplate(pipeline.getId(), dataMedia.getSource());
        int[] ints = dataSource.batchUpdate(sqls.toArray(new String[sqls.size()]));
        LogUtils.log(INFO, logger, () -> "=batchRetl=>table name =%s.%s , index name =%s,type=%s,insert size=%s"
                , () -> {
                    return new Object[]{wideTable.getSlaveTable().getNamespace()
                            , wideTable.getSlaveTable().getName(), wideTable.getTarget().getNamespace(), wideTable.getTarget().getName(),
                            ints.length};
                });
    }

    private List<String> getSqlsByCondition(List<String> list, Pipeline pipeline, WideTable wideTable, boolean master) {
        String tableName = master ? wideTable.getMainTablePkIdName().split(TABLE_SPLIT_SUFFIX)[0] : wideTable.getSlaveTableFkIdName().split(TABLE_SPLIT_SUFFIX)[0];
        String fullName = getFullName(tableName, pipeline);
        List<WideHelper.TableColumn> tableColumns = WideHelper.CURRICULUM_CONDTION_PKID_COMPENSATE.get(tableName);
        String pkid = tableColumns.stream().filter(tableColumn -> tableColumn.isPrimaryKey()).map(WideHelper.TableColumn::getSourceName).findFirst().orElse(null);
        String conditionColumnName = tableColumns.stream().filter(tableColumn -> tableColumn.getTargetName().equals(master ? wideTable.getMainTablePkIdName().split(TABLE_SPLIT_SUFFIX)[1] : wideTable.getSlaveTableFkIdName().split(TABLE_SPLIT_SUFFIX)[1]))
                .map(WideHelper.TableColumn::getSourceName).findFirst().orElse(null);
        List<String> sqls = new ArrayList<>();
        if (null != fullName && null != pkid && null != conditionColumnName) {
            list.forEach(value -> {
                if (String.class.isAssignableFrom(value.getClass())) {
                    sqls.add(String.format(RELT_CONDITION_SQL, pkid, fullName, fullName, conditionColumnName + "='" + value + "'"));
                } else {
                    sqls.add(String.format(RELT_CONDITION_SQL, pkid, fullName, fullName, conditionColumnName + "=" + value));
                }
            });
            LogUtils.log(DEBUG, logger, () -> "=doCompensate=>from index with condition sqls=%s", () -> {
                StringBuilder msg = new StringBuilder();
                sqls.forEach(s -> msg.append(s).append("\n"));
                return new Object[]{msg.toString()};
            });
        } else {
            LogUtils.log(ERROR, logger, () -> "=doCompensate=>from index with condition wideTable=%s", wideTable.toString());
        }
        return sqls;
    }

    private List<String> getSqls(List<String> list, Pipeline pipeline, WideTable wideTable, boolean master) {
        String fullName = getFullName(master ? wideTable.getMainTablePkIdName().split(TABLE_SPLIT_SUFFIX)[0] : wideTable.getSlaveTableFkIdName().split(TABLE_SPLIT_SUFFIX)[0], pipeline);
        List<String> sqls = new ArrayList<>();
        list.forEach(value -> {
            sqls.add(String.format(RELT_INSERT_SQL, value, fullName));
        });
        LogUtils.log(DEBUG, logger, () -> "=doCompensate=>from index sqls=%s", () -> {
            StringBuilder msg = new StringBuilder();
            sqls.forEach(s -> msg.append(s).append("\n"));
            return new Object[]{msg.toString()};
        });
        return sqls;
    }


    //sqls:pkid,name::
    public int batchRetl(Long pipelineId, String retlSqls) {
        Pipeline pipeline = pipelineService.findById(pipelineId);
        JdbcTemplate dataSource = dataSourceService.getJdbcTemplate(pipelineId, pipeline.getPairs().get(0).getSource().getSource());
        LogUtils.log(INFO, logger, () -> "=batchRetl=>retlSqls:%s", retlSqls);
        Arrays.stream(retlSqls.split("::")).forEach(sql -> {
            String[] fields = sql.split(",");
            dataSource.update(String.format(RELT_INSERT_SQL, fields[0], fields[1]));
        });
        return 1;
    }

    //sqls:pkid,pkid
    public int batchRetl(Long pipelineId, String ids, String fullName) {
        Pipeline pipeline = pipelineService.findById(pipelineId);
        JdbcTemplate dataSource = dataSourceService.getJdbcTemplate(pipelineId, pipeline.getPairs().get(0).getSource().getSource());
        LogUtils.log(INFO, logger, () -> "=batchRetl=>ids:%s", ids);
        Arrays.stream(ids.split(",")).forEach(id -> {
            dataSource.update(String.format(RELT_INSERT_SQL, id, fullName));
        });
        return 1;
    }


    public void setIndexService(IndexJsonService indexService) {
        this.indexService = indexService;
    }

    public void setWideTableService(WideTableService wideTableService) {
        this.wideTableService = wideTableService;
    }

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setDefaultIndexService(IndexService defaultIndexService) {
        this.defaultIndexService = defaultIndexService;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public boolean isRun() {
        return isRun;
    }

    public boolean isRunUpdate() {
        return isRunUpdate;
    }

    public void setRunUpdate(boolean runUpdate) {
        isRunUpdate = runUpdate;
    }

//    private static final String PAGE_SQL = "select %s from %s where %s limit %s ,%s ";

//    private List<HashMap> getList(JdbcTemplate jdbcTemplate, String fullName, int offset, int pageSize) {
//        return jdbcTemplate.queryForList(String.format(PAGE_SQL, "*", fullName, offset, pageSize), HashMap.class);
//    }

    //    private PageList<Map<String, Object>> getDataList(String cityId, DataMedia dataMedia, List<String> includeFields, int pageNo, Long startDate, Long endDate, String year) {
//        BoolQueryBuilder builder = QueryBuilders.boolQuery();
//        if (!StringUtils.isEmpty(cityId)) {
//            builder.filter(QueryBuilders.termQuery(NameFormatUtils.formatName(dataMedia.getNamespace()) + "_cityId", cityId));
//        }
//        if (!StringUtils.isEmpty(year)) {
//            builder.filter(QueryBuilders.termQuery("clazz_year", year));
//        }
//        if (startDate != null && endDate != null) {
//            // startDate =< and <endDate  2018-12-03 00:00:00  <= 2018-12-03 23:59:59
//            builder.filter(QueryBuilders.rangeQuery("esDateTime").gte(startDate).lte(endDate));
//        }
//        return defaultIndexService.getDataByPage(dataMedia.getNamespace(), dataMedia.getName(), builder, includeFields, pageNo, 100);
//    }

//    private void handleCurriculumForAllFields(DataMedia dataMedia, List<WideTable> wideTables, Pipeline pipeline, Long startDate, Long endDate, String year) {
//        String cityId = FieldHelper.getCityCode(pipeline.getPairs().get(0));
//        PageList<Map<String, Object>> pageList = getDataList(cityId, dataMedia, null, 0, startDate, endDate, year);
//        List<Map<String, Object>> list = pageList.getList();
//        list.forEach(map -> {
//            Map<String, Map<String, Object>> tableValues = Maps.newHashMap();
//            map.entrySet().forEach(entry -> {
//                if (null == tableValues.get(entry.getKey().split("_")[0])) {
//                    tableValues.put(entry.getKey().split("_")[0], new HashMap<>());
//                }
//                tableValues.get(entry.getKey().split("_")[0]).put(entry.getKey(), entry.getValue());
//            });
//            //
//
//        });
//    }


    public static void main(String[] args) {
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(2, new NamedThreadFactory("-" + "-compensate-wide-timer-"));
        timer.scheduleWithFixedDelay(() -> {
            System.out.println(Thread.currentThread().getName() + "-d-date=" + DateUtils.nowStr());
            new Random(10).nextInt();
            try {
                TimeUnit.SECONDS.sleep(9);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);


        try {
            TimeUnit.HOURS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
