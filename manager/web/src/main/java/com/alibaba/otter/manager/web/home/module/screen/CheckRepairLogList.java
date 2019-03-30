package com.alibaba.otter.manager.web.home.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.check.*;
import com.alibaba.otter.manager.biz.check.thread.ExecutorManager;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.utils.DateUtils;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.hwl.otter.clazz.repairlog.CheckRepairLogService;
import com.hwl.otter.clazz.repairlog.dal.dataobject.CheckRepairLogDo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class CheckRepairLogList {
    protected final static Logger logger = LoggerFactory.getLogger(CheckRepairLogList.class);
    @Resource(name = "checkRepairLogService")
    private CheckRepairLogService checkRepairLogService;

    @Resource(name = "channelService")
    private ChannelService channelService;

    @Resource(name = "checkService")
    private CheckService checkService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;

    @Resource(name = "wideIndexService")
    private WideIndexService wideIndexService;

    @Resource(name = "checkQuartz")
    private CheckQuartz checkQuartz;

    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                        @Param("operation") String operation, @Param("checkRepairId") Long checkRepairId,
                        @Param("sourceSchema") String sourceSchema, @Param("sourceTable") String sourceTable,
                        @Param("isSuccess") Integer isSuccess,
                        @Param("wideIndexFlag") boolean wideIndexRun,
                        Context context) throws Exception {
        LogUtils.log(LogUtils.WARN, logger, () -> "=CheckRepairLogList=>operation:%s , wideIndexRun:%s ", operation, wideIndexRun);

        // 检查和修复功能
        if (operation != null && (operation.equals("repair") || operation.equals("check") || OperateEnum.contains(operation))) {
            Future<String[]> checkFuture = null;
            Future<String[]> repairFuture = null;
            CheckRepairLogDo checkRepairLog = checkRepairLogService.findById(checkRepairId);
            if (checkRepairLog == null) {
                context.put("msg", "操作失败，未找到该ID对应的日志对象,checkRepairId：" + checkRepairId);
                return;
            }
            List<DataMediaPair> dmpLs = dataMediaPairService.listByPipelineId(checkRepairLog.getPipelineId());
            if (dmpLs != null) {
                for (DataMediaPair dataMediaPair : dmpLs) {
                    if (dataMediaPair.getSource().getNamespace().equals(checkRepairLog.getCheckSourceSchema()) &&
                            dataMediaPair.getSource().getName().equals(checkRepairLog.getCheckSourceTable())) {

                        Channel channel = channelService.findByPipelineId(checkRepairLog.getPipelineId());
                        Pipeline pipeline = pipelineService.findById(checkRepairLog.getPipelineId());
                        if (operation.equals("repair")) {
                            if (channel.getStatus().isStart()) {
                                if (pipeline.getParameters().getSkipFreedom()) { // 是否跳过自由门
                                    context.put("msg", "操作失败，该Pipeline跳过自由门操作！Pipeline：" + pipeline.getId());
                                } else {
                                    repairFuture = ExecutorManager.submit(new RepairThread(DateUtils.getDateStr(checkRepairLog.getRepairBeginDate(), "yyyy-MM-dd HH:mm:ss"),
                                            DateUtils.getDateStr(checkRepairLog.getRepairEndDate(), "yyyy-MM-dd HH:mm:ss"),
                                            pipeline, dataMediaPair, checkService));
                                    break;
                                }
                            } else {
                                context.put("msg", "操作失败，通道未启动！channel：" + channel.getId());
                            }
                        } else if (operation.equals("check")) {
                            checkFuture = ExecutorManager.submit(new CheckThread(checkService, dataMediaPair, pipeline, true,
                                    DateUtils.getDateStr(checkRepairLog.getRepairBeginDate(), "yyyy-MM-dd HH:mm:ss"),
                                    DateUtils.getDateStr(checkRepairLog.getRepairEndDate(), "yyyy-MM-dd HH:mm:ss")));
                            break;
                        } else {
                            repairFuture = ExecutorManager.submit(new RepairThread(DateUtils.getDateStr(checkRepairLog.getRepairBeginDate(), "yyyy-MM-dd HH:mm:ss"),
                                    DateUtils.getDateStr(checkRepairLog.getRepairEndDate(), "yyyy-MM-dd HH:mm:ss"),
                                    pipeline, dataMediaPair, checkService, OperateEnum.valueOfName(operation)));
                            break;
                        }

                    }
                }
            }

            if (checkFuture != null && operation != null && operation.equals("check")) {
                String[] result = checkFuture.get();
                if (StringUtils.isNotEmpty(result[3])) {
                    context.put("msg", "数据检查异常，msg:" + result[3]);
                } else {
                    context.put("msg", "Pipeline Name " + result[4] + " 源和目标数据比对:" + (Integer.valueOf(result[2]) == 0 ? "一致" : "不一致") + " ,源表数据量:" + result[0] + ", 目标表数据量:" + result[1]);
                }
            } else if (repairFuture != null && operation != null && OperateEnum.contains(operation)) {
                String[] result = repairFuture.get();
                if (result[1] != null) {
                    context.put("msg", result[1]);
                } else {
                    int r = Integer.valueOf(result[0]);
                    if (r == 0) {
                        context.put("msg", "Pipeline Name " + result[2] + " 数据修复" + (r == 0 ? "成功" : "失败"));
                    } else {
                        context.put("msg", "Pipeline Name " + result[2] + " 数据修复" + (r == 0 ? "成功" : "失败") + ",请检查管道状态或稍等一段时间后再次操作数据检查查看数据是否一致");
                    }
                }
            }

        } else if (operation != null && operation.equals("autoRepair")) {
            context.put("msg", autoRepair());
        } else if (operation != null && operation.equals("modifyWideIndex")) {
            modifyWideIndex();
        } else if (operation != null && operation.equals("AddCheckDataInfo")) {
            AddCheckDataInfo();
        } else if (operation != null && operation.equals("wideIndexRun")) {
            checkService.setCheckSpecialField(!wideIndexRun);
        }


        // 获取源schema列表
        List<String> sourceSchemaList = checkRepairLogService.getSourceSchemaList();
        // 获取源table列表
        List<String> sourceTableList = checkRepairLogService.getSourceTableList();


        Map<String, Object> condition = new HashMap<String, Object>();
        if ("请输入关键字".equals(searchKey)) {
            searchKey = "";
        }
        condition.put("searchKey", searchKey);
        if (StringUtils.isNotEmpty(sourceSchema)) {
            String[] temp = sourceSchema.split(" ");
            condition.put("checkSourceName", temp[0]);
            condition.put("checkSourceSchema", temp[1]);
        }
        condition.put("checkSourceTable", sourceTable);
        condition.put("repairIsSuccess", isSuccess);

        int count = checkRepairLogService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<CheckRepairLogDo> checkRepairLogDoLs = checkRepairLogService.listCheckTableRel(condition);


        context.put("checkRepairLogDoLs", checkRepairLogDoLs);
        context.put("sourceSchemaList", sourceSchemaList);
        context.put("sourceTableList", sourceTableList);
        context.put("sourceSchema", sourceSchema);
        context.put("sourceTable", sourceTable);
        context.put("isSuccess", isSuccess);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
        context.put("pageIndex", pageIndex);
        context.put("wideIndexFlag", wideIndexService.isRun());


    }


    /**
     * 一键修复
     *
     * @return
     */
    private String autoRepair() {
        String msg = "修复线程已启动，请稍后刷新查看修复状态！";
        try {
            List<CheckRepairLogDo> repairLogList = checkRepairLogService.getRepairFailData();

            // key:pipelineId
            Map<Long, List<CheckRepairLogDo>> repairMap = new HashMap<Long, List<CheckRepairLogDo>>();

            // 分组
            if (!CollectionUtils.isEmpty(repairLogList)) {
                for (CheckRepairLogDo log : repairLogList) {
                    List<CheckRepairLogDo> tempList = repairMap.get(log.getPipelineId());
                    if (CollectionUtils.isEmpty(tempList)) {
                        tempList = new ArrayList<CheckRepairLogDo>();
                        tempList.add(log);
                    } else {
                        tempList.add(log);
                    }
                    repairMap.put(log.getPipelineId(), tempList);
                }
            }

            for (Map.Entry<Long, List<CheckRepairLogDo>> map : repairMap.entrySet()) {
                List<DataMediaPair> dmpLs = dataMediaPairService.listByPipelineId(map.getKey());
                Channel channel = channelService.findByPipelineId(map.getKey());
                Pipeline pl = pipelineService.findById(map.getKey());
                if (dmpLs != null) {
                    List<CheckRepairLogDo> tempList = map.getValue();
                    for (DataMediaPair dmp : dmpLs) {
                        for (CheckRepairLogDo repairDo : tempList) {
                            if (dmp.getSource().getNamespace().equals(repairDo.getCheckSourceSchema()) &&
                                    dmp.getSource().getName().equals(repairDo.getCheckSourceTable())) {
                                if (channel.getStatus().isStart()) {
                                    if (!pl.getParameters().getSkipFreedom()) { // 是否跳过自由门, true 跳过
                                        String beginDate = DateUtils.getDateStr(repairDo.getRepairBeginDate(), "yyyy-MM-dd HH:mm:ss");
                                        String endDate = DateUtils.getDateStr(repairDo.getRepairEndDate(), "yyyy-MM-dd HH:mm:ss");
                                        Boolean flag = checkService.sourceDataCompareTargetData(beginDate, endDate, dmp, pl); // 判断是否已修复成功
                                        if (flag) {
                                            // 更新修复日志状态
                                            checkService.updateCheckRepairLogDoState(dmp, beginDate, endDate, 0);
                                        } else {
                                            ExecutorManager.submit(new RepairThread(beginDate, endDate, pl, dmp, checkService));
                                        }
                                    } else {
                                        msg = "修复线程已启动，请稍后刷新查看修复状态。有Pipeline跳过自由门功能无法正常修复！";
                                    }
                                }
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "一键修复异常，msg:" + e.getMessage();
        }

        return msg;
    }

    /**
     * 修复所有宽表的脏数据
     */
    private void modifyWideIndex() {
        wideIndexService.handleWideIndex();
    }

    private void AddCheckDataInfo() {
        checkQuartz.doCheck();
    }

}
