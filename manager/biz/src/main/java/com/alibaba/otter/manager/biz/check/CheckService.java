package com.alibaba.otter.manager.biz.check;

import com.alibaba.otter.common.push.datasource.DataSourceService;
import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.common.push.index.wide.config.*;
import com.alibaba.otter.manager.biz.check.exception.NotUseException;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.config.widetable.WideTableService;
import com.alibaba.otter.manager.biz.utils.DateUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper.termin.WarningTerminProcess;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.LoadRoute;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.page.PageList;
import com.alibaba.otter.shared.common.page.PaginatedList;
import com.alibaba.otter.shared.common.utils.DingtalkUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.common.utils.NameFormatUtils;
import com.alibaba.otter.shared.common.utils.TheadPoolUtils;
import com.hwl.otter.clazz.datacheck.DataCheckService;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;
import com.hwl.otter.clazz.repairlog.CheckRepairLogService;
import com.hwl.otter.clazz.repairlog.dal.dataobject.CheckRepairLogDo;
import com.hwl.otter.clazz.tablerel.CheckTableRelService;
import com.hwl.otter.clazz.tablerel.dal.dataobject.CheckTableRelDo;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

/**
 * @Description: 数据检查修改服务
 * @Author: tangdelong
 * @Date: 2018/6/21 11:15
 */
public class CheckService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String PAGE_SQL = "select %s from %s limit %s ,%s ";
    private static final String PAGE_SQL_ORDER = "select %s from %s order by %s desc  limit %s ,%s ";

    private static final String COUNT_SQL = "select SQL_NO_CACHE  count(1) from %s  ";

    public static final String RELT_INSERT_SQL = "INSERT INTO udip_retl.retl_buffer ( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME, GMT_MODIFIED) values " +
            "( now(), '%s', '0', 'I', '%s', now()) ";

    public static final String RELT_CONDITION_SQL = "INSERT INTO udip_retl.retl_buffer ( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME, GMT_MODIFIED) " +
            "( select now(), %s, '0', 'I', '%s', now() from %s where %s )";

    private DataCheckService dataCheckService;

    private DataSourceService dataSourceService;

    private CheckTableRelService checkTableRelService;

    private CheckRepairLogService checkRepairLogService;

    private SystemParameterService systemParameterService;

    private WarningTerminProcess warningTerminProcess;

    private IndexService defaultEsService;

    private WideTableService wideTableService;

    private PipelineService pipelineService;

    private IndexConfigServiceFactory indexConfigServiceFactory;

    private volatile boolean checkSpecialField = true;
    /**
     * 检查结束时间前置秒数
     */
    private int preTime = -90;

    /**
     * 判断源和目标是否数据同步一致
     *
     * @return true:同步一致，false:同步不一致
     */
    public boolean sourceDataCompareTargetData(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline) {
        Integer[] count = getSourceDataCountTargetDataCount(beginTime, endTime, dataMediaPair, pipeline);
        int sourceCount = count[0];
        int targetCount = count[1];

        if (sourceCount == targetCount) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 判断源和目标是否数据同步一致
     *
     * @return true:同步一致，false:同步不一致
     */
    public boolean sourceDataCompareTargetData(int sourceCount, int targetCount) {
        if (sourceCount <= targetCount) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获取源和目标数据量
     *
     * @return 0：源数据数，1：目标数据数
     */
    public Integer[] getSourceDataCountTargetDataCount(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline) {
        Integer[] resultLs = new Integer[2];
        String sourceNamespace = dataMediaPair.getSource().getNamespace();
        String sourceTableName = dataMediaPair.getSource().getName();
        DbMediaSource sourceMedia = (DbMediaSource) dataMediaPair.getSource().getSource();
        Long pairId = dataMediaPair.getId();
        CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(sourceTableName);
        if (sourceCheckTable == null) {
            String msg = LogUtils.format("=getSourceDataCountTargetDataCount=> pipelineName:%s has not table:%s ", pipeline.getName(), sourceTableName) + " 数据修复，源表:" + sourceTableName + "映射关系未配置！";
            sendWarningMessage(dataMediaPair.getPipelineId(), "check-config", msg);
            LogUtils.log(INFO, log, () -> "=getSourceDataCountTargetDataCount=> pipelineName:%s has not table:%s ", pipeline.getName(), sourceTableName);
            resultLs[0] = 1;
            resultLs[1] = 1;
            return resultLs;
//            throw new IllegalArgumentException(msg);
        }
        String sourceTimeField = sourceCheckTable.getTimeFieldName();

        // 区间数据个数获取
        String sourceSql = "select SQL_NO_CACHE count(1) from " + sourceNamespace + "." + sourceTableName + " where 1=1 ";
        if (StringUtils.isNotBlank(beginTime)) {
            sourceSql = sourceSql + " and " + sourceTimeField + " > '" + beginTime + "' ";
        }
        if (StringUtils.isNotBlank(endTime)) {
            sourceSql = sourceSql + " and " + sourceTimeField + " <= '" + endTime + "' ";
        }
        if (StringUtils.isNotEmpty(sourceCheckTable.getWhereSql())) {
            sourceSql += " and " + sourceCheckTable.getWhereSql();
        }

        // 获取数据源
        JdbcTemplate sourceJdbcTemplate = getJdbcTemplate(pairId, sourceMedia);
        resultLs[0] = sourceJdbcTemplate.queryForInt(sourceSql);
        resultLs[1] = getEsCount(beginTime, endTime, pipeline, dataMediaPair, sourceTimeField);
        return resultLs;
    }

    private int getEsCount(String beginTime, String endTime, Pipeline pipeline, DataMediaPair dataMediaPair, String sourceTimeField) {
        // 转换ES字段格式
        String[] indexType = getEsInfo(pipeline, dataMediaPair.getTarget().getName());
        if (indexType == null) {
            throw new NotUseException("ES index和type名称没有找到，数据修复只修复数据库到ES for field: " + sourceTimeField);
        }
        String indexField = null;
        if (null != FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0])) {
            indexField = FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0]).get(sourceTimeField);
        }
        if (null == indexField) {
            indexField = sourceTimeField;
            LogUtils.log(ERROR, log, () -> "The index:%s ,type:%s ,can not get field , so use field:%s", indexType[0], indexType[1], indexField);
        }
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        RangeQueryBuilder range = QueryBuilders.rangeQuery(NameFormatUtils.formatName(indexType[0]) + "_" + NameFormatUtils.formatName(indexField));
        if (StringUtils.isNotBlank(beginTime)) {
            // es 时间根式转换
            beginTime = beginTime.replace(" ", "T");
            range.gt(beginTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            // es 时间根式转换
            endTime = endTime.replace(" ", "T");
            range.lte(endTime);
        }
        if (StringUtils.isNotEmpty(beginTime) || StringUtils.isNotEmpty(endTime)) {
            builder.filter(range);
        }
        if (!FieldMapping.NO_CITY_INDEX.contains(indexType[0])) {
            builder.filter(QueryBuilders.termQuery(NameFormatUtils.formatName(indexType[0]) + "_cityId", FieldHelper.getCityCode(dataMediaPair)));
        }
        List<WideTable> wideTableList = wideTableService.listByTargetIdAndTableId(0L, 0L);
        WideTable mainWideTable = wideTableList.stream().filter(wideTable -> wideTable.getMainTable().getId().equals(dataMediaPair.getTarget().getId())).findFirst().orElse(null);
        if (null != mainWideTable) {
            builder.filter(QueryBuilders.termQuery(FieldMapping.WIDE_TABLE_SYNC_FIELD_ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED));
        }
        LogUtils.log(WARN, log, () -> "=getEsCount=>sql:%s", builder.toString());
        return defaultEsService.getCount(indexType[0], indexType[1], builder);
    }


    /**
     * 获同步目标ES index type 名称
     *
     * @param pipeline        所在管道对象
     * @param targetTableName 目标数据库名称
     * @return String[0]=index  String[1] = type
     */
    private String[] getEsInfo(Pipeline pipeline, String targetTableName) {
        String[] result = null;
        List<LoadRoute> loadRouteList = pipeline.getRoutes();
        if (!CollectionUtils.isEmpty(loadRouteList)) {
            result = new String[2];
            for (LoadRoute l : loadRouteList) {
                if (l.getTable().getName().equals(targetTableName)) {
                    if (l.getLoadDataMedia().getSource().getType().isEs()) {
                        result[0] = l.getLoadDataMedia().getNamespace();
                        result[1] = l.getLoadDataMedia().getName();
                        break;
                    }
                }
            }
        }

        return result;
    }

    public int insertRetl(Long pipelineId, String tableName) {
        Pipeline pipeline = pipelineService.findById(pipelineId);
        DataMediaPair dataMediaPair1 = pipeline.getPairs().stream().filter(dataMediaPair -> dataMediaPair.getSource().getName().equalsIgnoreCase(tableName)).findFirst().orElse(null);
        return insertRetl(null, null, dataMediaPair1, 0);
    }

    /**
     * 数据修补
     *
     * @return
     */
    public int insertRetl(String beginTime, String endTime, DataMediaPair dataMediaPair, Integer dataSum) {
        String sourceNamespace = dataMediaPair.getSource().getNamespace();
        String sourceTableName = dataMediaPair.getSource().getName();
        String sourceUrl = ((DbMediaSource) dataMediaPair.getSource().getSource()).getUrl();
        String targetNamespace = dataMediaPair.getTarget().getNamespace();
        String targetTableName = dataMediaPair.getTarget().getName();
        String targetUrl = ((DbMediaSource) dataMediaPair.getTarget().getSource()).getUrl();

        CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(sourceTableName);
        String sourceTimeField = sourceCheckTable.getTimeFieldName();
        String sourceWhereSql = sourceCheckTable.getWhereSql();
        String sourceKeyId = sourceCheckTable.getKeyName();

        // 自由门
        SystemParameter systemParameter = systemParameterService.find();
        String retlSchema = systemParameter.getSystemSchema();
        String retlTable = systemParameter.getSystemBufferTable();
        JdbcTemplate sourceJdbcTemplate = getJdbcTemplate(dataMediaPair.getPipelineId(), (DbMediaSource) dataMediaPair.getSource().getSource());
        String retlSql = "insert into " + retlSchema + "." + retlTable + "( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME,GMT_MODIFIED)" +
                "select now()," + sourceKeyId + ",0,'I','" + sourceNamespace + "." + sourceTableName + "',now() " +
                " from " + sourceNamespace + "." + sourceTableName;
        retlSql = retlSql + " where 1=1 ";
        if (StringUtils.isNotBlank(beginTime)) {
            retlSql = retlSql + " and " + sourceTimeField + " > '" + beginTime + "'";
        }
        if (StringUtils.isNotBlank(endTime)) {
            retlSql = retlSql + " and " + sourceTimeField + " <= '" + endTime + "' ";
        }

        if (StringUtils.isNotEmpty(sourceCheckTable.getWhereSql())) {
            retlSql += " and " + sourceCheckTable.getWhereSql();
        }

        log.info("====================================");
        log.info("数据修复-向retl库中插入数据SQL：" + retlSql);
        log.info("====================================");

        int insertCount = 0;
        if (dataSum != null && dataSum > 30000) {   // 源表数据量大于3W条进行分差录入
            Integer append = 1000;  // 每次增加数据量
            Integer limit1 = 0;
            Integer limit2 = append;

            String retlSql1;
            for (; limit1 < dataSum; ) {
                retlSql1 = retlSql + " limit " + limit1 + "," + limit2;
                int temp = sourceJdbcTemplate.update(retlSql1);
                insertCount += temp;
                limit1 += append;
            }
        } else {
            insertCount = sourceJdbcTemplate.update(retlSql);
        }

        return insertCount;
    }


    /**
     * 获取比对的开始和结束时间
     *
     * @param dataCheckDo
     * @return
     */
    public String[] getBeginEndTime(DataCheckDo dataCheckDo) {
        String[] result = new String[2];
        String beginTime = null;
        String endTime = null;
        if (dataCheckDo == null) {
            beginTime = null;
            endTime = null;
        } else {
            beginTime = DateUtils.getDateStr(dataCheckDo.getCheckEndDate(), "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtils.getCurrentDateAddSecond(preTime, "yyyy-MM-dd HH:mm:ss");
        }
        result[0] = beginTime;
        result[1] = endTime;
        return result;
    }


    /**
     * 获取数据检查对象
     *
     * @param dataMediaPair
     * @return
     */
    public DataCheckDo getDataCheckDo(DataMediaPair dataMediaPair) {
        //  数据检查对象获取
        DataCheckDo condition = new DataCheckDo();
        condition.setPipelineId(dataMediaPair.getPipelineId());
        condition.setCheckSourceSchema(dataMediaPair.getSource().getNamespace());
        condition.setCheckSourceTable(dataMediaPair.getSource().getName());
        condition.setCheckTargetSchema(dataMediaPair.getTarget().getNamespace());
        condition.setCheckTargetTable(dataMediaPair.getTarget().getName());
        List<DataCheckDo> dcls = dataCheckService.findByCondition(condition);
        DataCheckDo dc = null;
        if (!CollectionUtils.isEmpty(dcls)) {
            dc = dcls.get(0);
        }
        return dc;
    }


    /**
     * 获取修复日志信息
     *
     * @param dataMediaPair
     * @param beginTime
     * @param endTime
     * @return
     */
    public CheckRepairLogDo getCheckRepairLog(DataMediaPair dataMediaPair, String beginTime, String endTime) {
        CheckRepairLogDo crl = new CheckRepairLogDo();
        crl.setPipelineId(dataMediaPair.getPipelineId());
        crl.setCheckSourceSchema(dataMediaPair.getSource().getNamespace());
        crl.setCheckSourceTable(dataMediaPair.getSource().getName());
        crl.setCheckTargetSchema(dataMediaPair.getTarget().getNamespace());
        crl.setCheckTargetTable(dataMediaPair.getTarget().getName());
        crl.setRepairBeginDate(DateUtils.getTimestamp(beginTime, "yyyy-MM-dd HH:mm:ss"));
        crl.setRepairEndDate(DateUtils.getTimestamp(endTime, "yyyy-MM-dd HH:mm:ss"));
        List<CheckRepairLogDo> ls = checkRepairLogService.findCheckRepairLogDoByCondition(crl);
        if (CollectionUtils.isEmpty(ls)) {
            return null;
        } else {
            return ls.get(0);
        }
    }


    /**
     * 更新检查时间
     *
     * @param dc
     * @param dataMediaPair
     * @param endTime
     */
    public DataCheckDo updateDataCheckDo(DataCheckDo dc, Long channelId, DataMediaPair dataMediaPair,
                                         String beginTime, String endTime) {
        if (dc == null) {
            dc = new DataCheckDo();
            dc.setChannelId(channelId);
            dc.setPipelineId(dataMediaPair.getPipelineId());
            dc.setCheckSourceName(dataMediaPair.getSource().getSource().getName());
            dc.setCheckSourceSchema(dataMediaPair.getSource().getNamespace());
            dc.setCheckSourceTable(dataMediaPair.getSource().getName());
            dc.setCheckTargetName(dataMediaPair.getTarget().getSource().getName());
            dc.setCheckTargetSchema(dataMediaPair.getTarget().getNamespace());
            dc.setCheckTargetTable(dataMediaPair.getTarget().getName());
            dc.setCheckBeginDate(DateUtils.getTimestamp(beginTime, "yyyy-MM-dd HH:mm:ss"));
            dc.setCheckEndDate(DateUtils.getTimestamp(endTime, "yyyy-MM-dd HH:mm:ss"));
            dc.setIsStart(1);
//            dc.setRepairFailNum(repairFailNum);
            dataCheckService.insertDataCheckDo(dc);
        } else {
            dc.setCheckBeginDate(DateUtils.getTimestamp(beginTime, "yyyy-MM-dd HH:mm:ss"));
            dc.setCheckEndDate(DateUtils.getTimestamp(endTime, "yyyy-MM-dd HH:mm:ss"));
//            dc.setRepairFailNum(repairFailNum);
            dataCheckService.updateDataCheckDoById(dc);
        }

        return dc;
    }


    /**
     * 数据修复日志记录
     *
     * @param dataMediaPair
     * @param beginTime
     * @param endTime
     * @param repairNum
     * @param repairIsSuccess
     */
    public void insertRepairLog(Long channelId, DataMediaPair dataMediaPair, String beginTime, String endTime, int repairNum, int repairIsSuccess) {
        CheckRepairLogDo cr = new CheckRepairLogDo();
        cr.setChannelId(channelId);
        cr.setPipelineId(dataMediaPair.getPipelineId());
        cr.setCheckSourceName(dataMediaPair.getSource().getSource().getName());
        cr.setCheckSourceSchema(dataMediaPair.getSource().getNamespace());
        cr.setCheckSourceTable(dataMediaPair.getSource().getName());
        cr.setCheckTargetName(dataMediaPair.getTarget().getSource().getName());
        cr.setCheckTargetSchema(dataMediaPair.getTarget().getNamespace());
        cr.setCheckTargetTable(dataMediaPair.getTarget().getName());
        cr.setRepairBeginDate(DateUtils.getTimestamp(beginTime, "yyyy-MM-dd HH:mm:ss"));
        cr.setRepairEndDate(DateUtils.getTimestamp(endTime, "yyyy-MM-dd HH:mm:ss"));
        cr.setRepairNum(repairNum);
        cr.setRepairIsSuccess(repairIsSuccess);
        checkRepairLogService.insertcheckRepairLogDo(cr);
    }


    // 修改 修复记录状态 0:成功，1:失败
    public void updateCheckRepairLogDoState(DataMediaPair dataMediaPair, String beginTime, String endTime, int state) {
        CheckRepairLogDo crl = new CheckRepairLogDo();
        crl.setPipelineId(dataMediaPair.getPipelineId());
        crl.setCheckSourceSchema(dataMediaPair.getSource().getNamespace());
        crl.setCheckSourceTable(dataMediaPair.getSource().getName());
        crl.setCheckTargetSchema(dataMediaPair.getTarget().getNamespace());
        crl.setCheckTargetTable(dataMediaPair.getTarget().getName());
        crl.setRepairBeginDate(DateUtils.getTimestamp(beginTime, "yyyy-MM-dd HH:mm:ss"));
        crl.setRepairEndDate(DateUtils.getTimestamp(endTime, "yyyy-MM-dd HH:mm:ss"));
        crl.setRepairIsSuccess(state);
        checkRepairLogService.updatecheckRepairLogDoByCondition(crl);
    }


    /**
     * 根据pepelineId和dbMediaSource获取JdbcTemplate
     *
     * @param pepelineId
     * @param dbMediaSource
     * @return
     */
    public JdbcTemplate getJdbcTemplate(Long pepelineId, DbMediaSource dbMediaSource) {
        return dataSourceService.getJdbcTemplate(pepelineId, dbMediaSource);
    }


    public String jdbcUrlProcesser(String url, String nameSpace) {
        url = url.replace("jdbc:mysql://", "");
        url += "/" + nameSpace;
        return url;
    }


//    public DBCodeRelDo getDBCodeRel(String url, String nameSpace, String tableName) {
//        DBCodeRelDo condition = new DBCodeRelDo();
//        condition.setDbUrl(jdbcUrlProcesser(url, nameSpace));
//        condition.setTableName(tableName);
//        DBCodeRelDo codeRelDo = dbCodeRelService.getByCondition(condition);
//        return codeRelDo;
//    }


    public void sendRepairWarningMessage(Long pipelineId, DataMediaPair dataMediaPair,
                                         String beginTime, String endTime, Integer[] count) {
        String sourceNamespace = dataMediaPair.getSource().getNamespace();
        String sourceTableName = dataMediaPair.getSource().getName();
        String sourceUrl = ((DbMediaSource) dataMediaPair.getSource().getSource()).getUrl();
        String targetNamespace = dataMediaPair.getTarget().getNamespace();
        String targetTableName = dataMediaPair.getTarget().getName();
        String targetUrl = ((DbMediaSource) dataMediaPair.getTarget().getSource()).getUrl();
        String msg = "触发自动修复功能, 源库表信息:" + sourceUrl + "/" + sourceNamespace + "." + sourceTableName +
                ",目标表信息:" + targetUrl + "/" + targetNamespace + "." + targetTableName +
                ",修复时间段：" + beginTime + " --- " + endTime + "," + "源表数据数量：" + count[0] + ",目标表数据量:" + count[1];

        sendWarningMessage(pipelineId, "Repair", msg);
    }


    /**
     * @param pipelineId
     * @param exceptionStr 异常名称
     * @param msg          异常消息体
     */
    public void sendWarningMessage(Long pipelineId, String exceptionStr, String msg) {
        TerminEventData eventData = new TerminEventData();
        eventData.setPipelineId(pipelineId);
        eventData.setType(TerminEventData.TerminType.WARNING);
        eventData.setCode(exceptionStr);
        eventData.setDesc(msg);
        DingtalkUtils.sendMsg("udip-manager", LogUtils.format("=sendWarningMessage=>Pipeline id %s,exception:%s , msg:%s ", pipelineId, exceptionStr, msg));
        warningTerminProcess.process(eventData);
    }

    /**
     * 查询缺失的id；
     *
     * @param pipelineId
     * @param targetTableName
     * @param startPage
     * @param maxPage
     * @return
     */
    public List<String> findOmitDatas(Long pipelineId, String targetTableName, int startPage, int maxPage, int pageSize) {
        List<String> notExistsEsList = Lists.newArrayList();
        Pipeline pipeline = pipelineService.findById(pipelineId);
        DataMediaPair dataMediaPair = pipeline.getPairs().stream()
                .filter(dataMediaPairParam -> dataMediaPairParam.getTarget().getName().equalsIgnoreCase(targetTableName))
                .findFirst().orElse(null);
        if (null == dataMediaPair) {
            return Lists.newArrayList("no table for " + targetTableName);
        }
        DataMedia source = dataMediaPair.getSource();
        JdbcTemplate jdbcTemplate = getJdbcTemplate(pipelineId, (DbMediaSource) source.getSource());
        CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(source.getName());
        String[] indexType = getEsInfo(pipeline, targetTableName);
        String indexField = null;
        if (null != FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0])) {
            indexField = FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0]).get(sourceCheckTable.getKeyName());
        }
        String fullName = source.getNamespace() + "." + source.getName();
        String pkidName = NameFormatUtils.formatName(indexType[0]) + "_" + NameFormatUtils.formatName(indexField);

        PageList<String> page = getPkidPage(jdbcTemplate, sourceCheckTable.getKeyName(), fullName, startPage, pageSize);
        List<String> existIds = defaultEsService.getExistIds(indexType[0], indexType[1]
                , pkidName, page.getList());
        if (page.getList().size() != existIds.size()) {
            page.getList().removeAll(existIds);
            notExistsEsList.addAll(page.getList());
        }
        existIds.clear();
        for (int i = startPage + 1; i <= maxPage && i <= page.getTotalPage(); i++) {
            page = getPkidPage(jdbcTemplate, sourceCheckTable.getKeyName(), fullName, i, pageSize);
            existIds = defaultEsService.getExistIds(NameFormatUtils.formatName(indexType[0]), NameFormatUtils.formatName(indexType[1])
                    , pkidName, page.getList());
            if (page.getList().size() != existIds.size()) {
                page.getList().removeAll(existIds);
                notExistsEsList.addAll(page.getList());
            }
            page.getList().clear();
            existIds.clear();

        }
        return notExistsEsList;
    }

    public void checkDirtyData(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline) {
        String[] indexType = getEsInfo(pipeline, dataMediaPair.getTarget().getName());
        if (checkSpecialField && indexType[0].equals(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME)
                || indexType[0].equals(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME)) {
            LogUtils.log(INFO, log, () -> "=checkDirtyData=>check special fields index :%s , beginTime:%s , endTime:%s ", indexType[0], null == beginTime ? "null" : beginTime, null == endTime ? "null" : endTime);
            CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(dataMediaPair.getSource().getName());
            if (sourceCheckTable == null) {
                String msg = "数据修复，源表:" + sourceCheckTable + "映射关系未配置！";
                sendWarningMessage(dataMediaPair.getPipelineId(), "check-config", msg);
                throw new IllegalArgumentException(msg);
            }
            String sourceTimeField = sourceCheckTable.getTimeFieldName();
            String indexField = null;
            if (null != FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0])) {
                indexField = FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0]).get(sourceTimeField);
            }
            if (null == indexField) {
                indexField = sourceTimeField;
                LogUtils.log(ERROR, log, () -> "The index:%s ,type:%s ,can not get field , so use field:%s", indexType[0], indexType[1], indexField);
            }
            BoolQueryBuilder builder = QueryBuilders.boolQuery();
            RangeQueryBuilder range = QueryBuilders.rangeQuery(NameFormatUtils.formatName(indexType[0]) + "_" + NameFormatUtils.formatName(indexField));
            if (StringUtils.isNotBlank(beginTime)) {
                // es 时间根式转换
                beginTime = beginTime.replace(" ", "T");
                range.gt(beginTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                // es 时间根式转换
                endTime = endTime.replace(" ", "T");
                range.lte(endTime);
            }
            if (StringUtils.isNotEmpty(beginTime) || StringUtils.isNotEmpty(endTime)) {
                builder.filter(range);
            }
            builder.filter(QueryBuilders.termQuery(NameFormatUtils.formatName(indexType[0]) + "_cityId", FieldHelper.getCityCode(dataMediaPair)));

            List<String> needUpdateFields = indexConfigServiceFactory.getNeedUpdateFields(indexType[0]);
            if (indexType[0].equals(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME)) {
                needUpdateFields.add(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT);
            }
            needUpdateFields.add(indexConfigServiceFactory.getPkidFormateMap(indexType[0]));
            int pageNo = 0;
            int pageSize = 1000;
            PageList<Map<String, Object>> page = insertRetl(dataMediaPair, pipeline, indexType, builder, needUpdateFields, pageNo, pageSize);
            for (int i = 1; i <= page.getTotalPage(); i++) {
                page = insertRetl(dataMediaPair, pipeline, indexType, builder, needUpdateFields, i, pageSize);
            }

            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }
            if (checkSpecial(dataMediaPair, indexType, builder, needUpdateFields)) {
                checkAndRecordLog(beginTime, endTime, dataMediaPair, pipeline);
            }
        }
    }

    private PageList<Map<String, Object>> insertRetl(DataMediaPair dataMediaPair, Pipeline pipeline, String[] indexType, BoolQueryBuilder builder, List<String> needUpdateFields, int pageNo, int pageSize) {
        List<WideTable> wideTables = wideTableService.listByTargetIdAndTableId(-1L, dataMediaPair.getTarget().getId());
        PageList<Map<String, Object>> page = defaultEsService.getDataByPage(indexType[0], indexType[1], builder, needUpdateFields, pageNo, pageSize);
        List<Map<String, Object>> dataList = page.getList().stream().filter(map -> indexConfigServiceFactory.isDirtyFieldMap(indexType[0], map, wideTables)).collect(Collectors.toList());
        Optional.ofNullable(dataList).ifPresent(datas -> {
            List<String> ids = datas.stream().map(map -> map.get(wideTables.get(0).getMainTablePkIdName()).toString()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(ids)) {
                DingtalkUtils.sendMsg("udip-manager", LogUtils.format("=insertRetl=>Pipeline name %s have dirty data need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), indexType[0], ids.size()));
                LogUtils.log(INFO, log, () -> "=insertRetl=>Pipeline name %s have dirty data need handle ,the wide index %s have %s rows need to modify.", pipeline.getName(), indexType[0], ids.size());
                List<String> sqls = getSqls(ids, dataMediaPair.getSource().getNamespace() + "." + dataMediaPair.getSource().getName());
                batchRetl(pipeline, wideTables.get(0), sqls);//FIXME 拼接sql
            }
        });
        return page;
    }

    private boolean checkSpecial(DataMediaPair dataMediaPair, String[] indexType, BoolQueryBuilder builder, List<String> needUpdateFields) {
        int pageNo = 0;
        int pageSize = 1000;
        List<WideTable> wideTables = wideTableService.listByTargetIdAndTableId(-1L, dataMediaPair.getTarget().getId());
        PageList<Map<String, Object>> page = defaultEsService.getDataByPage(indexType[0], indexType[1], builder, needUpdateFields, pageNo, pageSize);
        List<Map<String, Object>> dataList = page.getList().stream().filter(map -> indexConfigServiceFactory.isDirtyFieldMap(indexType[0], map, wideTables)).collect(Collectors.toList());
        boolean isok = true;
        if (!CollectionUtils.isEmpty(dataList)) {
            isok = isok && true;
        }
        for (int i = 1; i <= page.getTotalPage(); i++) {
            page = defaultEsService.getDataByPage(indexType[0], indexType[1], builder, needUpdateFields, i, pageSize);
            dataList = page.getList().stream().filter(map -> indexConfigServiceFactory.isDirtyFieldMap(indexType[0], map, wideTables)).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(dataList)) {
                isok = isok && true;
            }
            dataList.clear();
        }
        return isok;
    }

    private void batchRetl(Pipeline pipeline, WideTable wideTable, List<String> sqls) {
        DataMedia dataMedia = ConfigHelper.findSourceDataMedia(pipeline, wideTable.getSlaveTable().getId());
        JdbcTemplate dataSource = dataSourceService.getJdbcTemplate(pipeline.getId(), dataMedia.getSource());
        int[] ints = dataSource.batchUpdate(sqls.toArray(new String[sqls.size()]));
        LogUtils.log(INFO, log, () -> "=batchRetl=>table name =%s.%s , index name =%s,type=%s,insert size=%s", () ->
                new Object[]{wideTable.getSlaveTable().getNamespace(), wideTable.getSlaveTable().getName(), wideTable.getTarget().getNamespace(), wideTable.getTarget().getName(), ints.length});
    }


    private List<String> getSqls(List<String> list, String fullName) {
        List<String> sqls = new ArrayList<>();
        list.forEach(value -> sqls.add(String.format(RELT_INSERT_SQL, value, fullName)));
        LogUtils.log(DEBUG, log, () -> "=getSqls=>from index sqls=%s", () -> {
            StringBuilder msg = new StringBuilder();
            sqls.forEach(s -> msg.append(s).append("\n"));
            return new Object[]{msg.toString()};
        });
        return sqls;
    }


    public String[] addOmitDatas(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline) {
        int pageSize = 1000;
        int startPage = 0;
        PageList<String> page = getPkidPageFromDB(beginTime, endTime, dataMediaPair, pipeline, startPage, pageSize);
        List<String> existIds = getExistIdsByList(beginTime, endTime, dataMediaPair, pipeline, page);
        insertOmitDatas(dataMediaPair, pipeline, page, existIds);
        existIds.clear();
        List<CompletableFuture> list = Lists.newArrayList();
        for (int i = 1; i <= page.getTotalPage(); i++) {
            LogUtils.log(INFO, log, () -> "=addOmitDatas=>pageNo:%s ,beginTime:%s ,endTime:%s ,pipelineName:%s", String.valueOf(i), beginTime, endTime, pipeline.getName());
            int pageNo = i;
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                PageList<String> pageList = getPkidPageFromDB(beginTime, endTime, dataMediaPair, pipeline, pageNo, pageSize);
                insertOmitDatas(dataMediaPair, pipeline, pageList, getExistIdsByList(beginTime, endTime, dataMediaPair, pipeline, pageList));
                return null;
            }, TheadPoolUtils.getInstance().executors);
            list.add(future);
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
        }
        LogUtils.log(INFO, log, () -> "<=addOmitDatas=>beginTime:%s ,endTime:%s ,pipelineName:%s", beginTime, endTime, pipeline.getName());
        return checkAndRecordLog(beginTime, endTime, dataMediaPair, pipeline);
    }

    private String[] checkAndRecordLog(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline) {
        String[] result = new String[3];
        int repairFailNum = 1;
        CheckRepairLogDo checkRepairLog = getCheckRepairLog(dataMediaPair, beginTime, endTime);
        if (checkRepairLog != null) {
            repairFailNum = checkRepairLog.getRepairNum() + 1;
        }
        boolean compare1 = sourceDataCompareTargetData(beginTime, endTime, dataMediaPair, pipeline);
        result[0] = compare1 ? "0" : "1";
        if (compare1) {
            insertRepairLog(pipeline.getChannelId(), dataMediaPair, beginTime, endTime, repairFailNum, 0); // 记录成功修复日志

            // 更改同条件下的状态为成功
            updateCheckRepairLogDoState(dataMediaPair, beginTime, endTime, 0);
        } else {
            insertRepairLog(pipeline.getChannelId(), dataMediaPair, beginTime, endTime, repairFailNum, 1); // 记录失败修复日志
            result[1] = "=checkAndRecordLog=>数据修复失败,请尝试手动触发修复！";
            log.error("=checkAndRecordLog=>数据修复失败,请尝试手动触发修复！");
        }
        result[2] = pipeline.getName();
        return result;
    }

    private void insertOmitDatas(DataMediaPair dataMediaPair, Pipeline pipeline, PageList<String> dbPageList, List<String> existIds) {
        if (dbPageList.getList().size() != existIds.size()) {
            dbPageList.getList().removeAll(existIds);
            List<String> datas = Lists.newArrayList(dbPageList.getList());
            datas.forEach(id -> LogUtils.log(WARN, log, () -> "=insertOmitDatas=>id:%s", id));
            String fullName = dataMediaPair.getSource().getNamespace() + "." + dataMediaPair.getSource().getName();
            List<String> sqls = datas.stream().map(id -> String.format(RELT_INSERT_SQL, id, fullName)).collect(Collectors.toList());
            sqls.forEach(sql -> {
                LogUtils.log(WARN, log, () -> "=insertOmitDatas=>sql:%s", sql);//merge sql
                JdbcTemplate jdbcTemplate = getJdbcTemplate(pipeline.getId(), (DbMediaSource) dataMediaPair.getSource().getSource());
                jdbcTemplate.update(sql);
            });
        }
    }

    private List<String> getExistIdsByList(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline, PageList<String> page) {
        CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(dataMediaPair.getSource().getName());
        if (sourceCheckTable == null) {
            String msg = "数据修复，源表:" + dataMediaPair.getSource().getName() + "映射关系未配置！";
            sendWarningMessage(dataMediaPair.getPipelineId(), "check-config", msg);
            throw new IllegalArgumentException(msg);
        }
        String[] indexType = getEsInfo(pipeline, dataMediaPair.getTarget().getName());
        String sourceTimeField = sourceCheckTable.getTimeFieldName();
        String indexField = null;
        if (null != FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0])) {
            indexField = FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0]).get(sourceTimeField);
        }
        if (null == indexField) {
            indexField = sourceTimeField;
            LogUtils.log(ERROR, log, () -> "=getExistIdsByList=>The index:%s ,type:%s ,can not get field , so use field:%s", indexType[0], indexType[1], indexField);
        }
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        RangeQueryBuilder range = QueryBuilders.rangeQuery(NameFormatUtils.formatName(indexType[0]) + "_" + NameFormatUtils.formatName(indexField));
        if (StringUtils.isNotBlank(beginTime)) {
            // es 时间根式转换
            beginTime = beginTime.replace(" ", "T");
            range.gt(beginTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            // es 时间根式转换
            endTime = endTime.replace(" ", "T");
            range.lte(endTime);
        }
        if (StringUtils.isNotEmpty(beginTime) || StringUtils.isNotEmpty(endTime)) {
            builder.filter(range);
        }
        String pkidName = indexConfigServiceFactory.getPkidFormateMap(indexType[0]);
        return defaultEsService.getExistIds(indexType[0], indexType[1], pkidName, builder, page.getList());
    }

    private PageList<String> getPkidPageFromDB(String beginTime, String endTime, DataMediaPair dataMediaPair, Pipeline pipeline, int startPage, int pageSize) {
        DataMedia source = dataMediaPair.getSource();
        JdbcTemplate jdbcTemplate = getJdbcTemplate(pipeline.getId(), (DbMediaSource) source.getSource());

        String fullName = source.getNamespace() + "." + source.getName();
        CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(source.getName());
        if (null == sourceCheckTable) {
            throw new RuntimeException("No Config Table for " + source.getName());
        }
        String whereSql = " where 1=1 ";
        if (StringUtils.isNotBlank(beginTime)) {
            whereSql = whereSql + " and " + sourceCheckTable.getTimeFieldName() + " > '" + beginTime + "' ";
        }
        if (StringUtils.isNotBlank(endTime)) {
            whereSql = whereSql + " and " + sourceCheckTable.getTimeFieldName() + " <= '" + endTime + "' ";
        }
        if (StringUtils.isNotEmpty(sourceCheckTable.getWhereSql())) {
            whereSql += " and " + sourceCheckTable.getWhereSql();
        }
        return getPkidPage(jdbcTemplate, sourceCheckTable.getKeyName(), fullName, whereSql, startPage, pageSize);
    }

    public List<String> addOmitDatas(Long pipelineId, String targetTableName, int startPage, int maxPage, int pageSize) {
        List<String> notExistsEsList = Lists.newArrayList();
        Pipeline pipeline = pipelineService.findById(pipelineId);
        DataMediaPair dataMediaPair = pipeline.getPairs().stream()
                .filter(dataMediaPairParam -> dataMediaPairParam.getTarget().getName().equalsIgnoreCase(targetTableName))
                .findFirst().orElse(null);
        if (null == dataMediaPair) {
            return Lists.newArrayList("no table for " + targetTableName);
        }
        DataMedia source = dataMediaPair.getSource();
        JdbcTemplate jdbcTemplate = getJdbcTemplate(pipelineId, (DbMediaSource) source.getSource());
        CheckTableRelDo sourceCheckTable = checkTableRelService.findCheckTableRelByTableName(source.getName());
        String[] indexType = getEsInfo(pipeline, targetTableName);
        String indexField = null;
        if (null != FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0])) {
            indexField = FieldMapping.INDEX_FIELD_MAPPING.get(indexType[0]).get(sourceCheckTable.getKeyName());
        }
        String fullName = source.getNamespace() + "." + source.getName();
        String pkidName = NameFormatUtils.formatName(indexType[0]) + "_" + NameFormatUtils.formatName(indexField);

        PageList<String> page = getPkidPage(jdbcTemplate, sourceCheckTable.getKeyName(), fullName, startPage, pageSize);
        List<String> existIds = defaultEsService.getExistIds(indexType[0], indexType[1], pkidName, page.getList());
        if (page.getList().size() != existIds.size()) {
            page.getList().removeAll(existIds);
            notExistsEsList.addAll(page.getList());
        }
        existIds.clear();
        for (int i = startPage + 1; i <= maxPage && i <= page.getTotalPage(); i++) {
            page = getPkidPage(jdbcTemplate, sourceCheckTable.getKeyName(), fullName, i, pageSize);
            existIds = defaultEsService.getExistIds(NameFormatUtils.formatName(indexType[0]), NameFormatUtils.formatName(indexType[1])
                    , pkidName, page.getList());
            if (page.getList().size() != existIds.size()) {
                page.getList().removeAll(existIds);
                notExistsEsList.addAll(page.getList());
                List<String> datas = Lists.newArrayList(page.getList());
                CompletableFuture.supplyAsync(() -> {
                    List<String> sqls = datas.stream().map(id -> String.format(WideIndexService.RELT_INSERT_SQL, id, fullName)).collect(Collectors.toList());
                    sqls.forEach(sql -> {
                        LogUtils.log(WARN, log, () -> "=addOmitDatas=>sql:%s", sql);
                        jdbcTemplate.update(sql);
                    });
                    return null;
                });
            }
        }
        return notExistsEsList;
    }


    private PageList<String> getPkidPage(JdbcTemplate jdbcTemplate, String pkid, String fullName, int pageNo, int pageSize) {
        PageList<String> page = new PaginatedList(pageNo, pageSize);
        page.setTotal(getCount(jdbcTemplate, fullName));
        page.addAll(getPkid(jdbcTemplate, pkid, fullName, page.getStartRow(), pageSize));
        return page;
    }

    private PageList<String> getPkidPage(JdbcTemplate jdbcTemplate, String pkid, String fullName, String whereSql, int pageNo, int pageSize) {
        PageList<String> page = new PaginatedList(pageNo, pageSize);
        page.setTotal(getCount(jdbcTemplate, fullName + " " + whereSql));
        page.addAll(getPkid(jdbcTemplate, pkid, fullName + " " + whereSql, page.getStartRow(), pageSize));
        return page;
    }

    private List<String> getPkid(JdbcTemplate jdbcTemplate, String pkid, String fullName, int offset, int pageSize) {
        return jdbcTemplate.queryForList(String.format(PAGE_SQL, pkid, fullName, offset, pageSize), String.class);
    }

    private int getCount(JdbcTemplate jdbcTemplate, String fullName) {
        return jdbcTemplate.queryForObject(String.format(COUNT_SQL, fullName), Integer.class);
    }

    public DataCheckService getDataCheckService() {
        return dataCheckService;
    }

    public void setDataCheckService(DataCheckService dataCheckService) {
        this.dataCheckService = dataCheckService;
    }

    public DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public CheckTableRelService getCheckTableRelService() {
        return checkTableRelService;
    }

    public void setCheckTableRelService(CheckTableRelService checkTableRelService) {
        this.checkTableRelService = checkTableRelService;
    }

    public CheckRepairLogService getCheckRepairLogService() {
        return checkRepairLogService;
    }

    public void setCheckRepairLogService(CheckRepairLogService checkRepairLogService) {
        this.checkRepairLogService = checkRepairLogService;
    }


    public SystemParameterService getSystemParameterService() {
        return systemParameterService;
    }

    public void setSystemParameterService(SystemParameterService systemParameterService) {
        this.systemParameterService = systemParameterService;
    }

    public void setIndexConfigServiceFactory(IndexConfigServiceFactory indexConfigServiceFactory) {
        this.indexConfigServiceFactory = indexConfigServiceFactory;
    }

    public WarningTerminProcess getWarningTerminProcess() {
        return warningTerminProcess;
    }

    public void setWarningTerminProcess(WarningTerminProcess warningTerminProcess) {
        this.warningTerminProcess = warningTerminProcess;
    }

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setWideTableService(WideTableService wideTableService) {
        this.wideTableService = wideTableService;
    }

    public IndexService getDefaultEsService() {
        return defaultEsService;
    }

    public void setDefaultEsService(IndexService defaultEsService) {
        this.defaultEsService = defaultEsService;
    }

    public boolean isCheckSpecialField() {
        return checkSpecialField;
    }

    public void setCheckSpecialField(boolean checkSpecialField) {
        this.checkSpecialField = checkSpecialField;
    }
}
