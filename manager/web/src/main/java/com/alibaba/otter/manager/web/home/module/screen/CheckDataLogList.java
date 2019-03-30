package com.alibaba.otter.manager.web.home.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.util.Paginator;
import com.alibaba.otter.manager.biz.check.CheckService;
import com.alibaba.otter.manager.biz.check.CheckThread;
import com.alibaba.otter.manager.biz.check.OperateEnum;
import com.alibaba.otter.manager.biz.check.RepairThread;
import com.alibaba.otter.manager.biz.check.thread.ExecutorManager;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.utils.DateUtils;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.hwl.otter.clazz.datacheck.DataCheckService;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class CheckDataLogList {

    @Resource(name = "dataCheckService")
    public DataCheckService dataCheckService;

    @Resource(name = "channelService")
    private ChannelService channelService;

    @Resource(name = "checkService")
    private CheckService checkService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "pipelineService")
    private PipelineService pipelineService;


    public void execute(@Param("pageIndex") int pageIndex, @Param("searchKey") String searchKey,
                        @Param("operation") String operation, @Param("id") Long id,
                        @Param("sourceSchema") String sourceSchema, @Param("targetSchema") String targetSchema,
                        Context context) throws Exception {


        Future<String[]> checkFuture = null;
        Future<String[]> repairFuture = null;
        if (operation != null && (operation.equals("repair") || operation.equals("check") || OperateEnum.contains(operation))) {
            DataCheckDo dataCheckDo = dataCheckService.getCheckDataLogById(id);
            if (dataCheckDo == null) {
                context.put("msg", "操作失败，未找到该ID对应的日志对象,id：" + id);
                return;
            }

            List<DataMediaPair> dmpLs = dataMediaPairService.listByPipelineId(dataCheckDo.getPipelineId());
            if (dmpLs != null) {
                for (DataMediaPair dataMediaPair : dmpLs) {
                    if (dataMediaPair.getSource().getNamespace().equals(dataCheckDo.getCheckSourceSchema()) &&
                            dataMediaPair.getSource().getName().equals(dataCheckDo.getCheckSourceTable())) {

                        Channel channel = channelService.findByPipelineId(dataCheckDo.getPipelineId());
                        Pipeline pipeline = pipelineService.findById(dataCheckDo.getPipelineId());
                        if (operation.equals("repair")) {
                            if (channel.getStatus().isStart()) {
                                if (pipeline.getParameters().getSkipFreedom()) { // 是否跳过自由门
                                    context.put("msg", "操作失败，该Pipeline跳过自由门操作！Pipeline：" + pipeline.getId());
                                } else {
                                    repairFuture = ExecutorManager.submit(new RepairThread(DateUtils.getDateStr(dataCheckDo.getCheckBeginDate(), "yyyy-MM-dd HH:mm:ss"),
                                            DateUtils.getDateStr(dataCheckDo.getCheckEndDate(), "yyyy-MM-dd HH:mm:ss"),
                                            pipeline, dataMediaPair, checkService));
                                    break;
                                }
                            } else {
                                context.put("msg", "操作失败，通道未启动！channel：" + channel.getId());
                            }
                        } else if (operation.equals("check")) {
                            checkFuture = ExecutorManager.submit(new CheckThread(checkService, dataMediaPair, pipeline, true,
                                    DateUtils.getDateStr(dataCheckDo.getCheckBeginDate(), "yyyy-MM-dd HH:mm:ss"),
                                    DateUtils.getDateStr(dataCheckDo.getCheckEndDate(), "yyyy-MM-dd HH:mm:ss")));
                            break;
                        } else {
                            repairFuture = ExecutorManager.submit(new RepairThread(DateUtils.getDateStr(dataCheckDo.getCheckBeginDate(), "yyyy-MM-dd HH:mm:ss"),
                                    DateUtils.getDateStr(dataCheckDo.getCheckEndDate(), "yyyy-MM-dd HH:mm:ss"),
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


        } else if (operation != null && operation.equals("isStart")) {
            DataCheckDo dataCheckDo = dataCheckService.getCheckDataLogById(id);
            dataCheckDo.setIsStart(dataCheckDo.getIsStart() == 0 ? 1 : 0);
            dataCheckService.updateDataCheckDoById(dataCheckDo);
        }


        // 获取源schema列表
        List<String> sourceSchemaList = dataCheckService.getSourceSchemaList();
        // 获目标schema列表
        List<String> targetSchemaList = dataCheckService.getTargetSchemaList();


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
        if (StringUtils.isNotEmpty(targetSchema)) {
            String[] temp = targetSchema.split(" ");
            condition.put("checkTargetName", temp[0]);
            condition.put("checkTargetSchema", temp[1]);
        }


        int count = dataCheckService.getCount(condition);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(pageIndex);

        condition.put("offset", paginator.getOffset());
        condition.put("length", paginator.getLength());

        List<DataCheckDo> dataCheckDoLs = dataCheckService.listCheckDataLogRel(condition);


        context.put("dataCheckDoLs", dataCheckDoLs);
        context.put("sourceSchemaList", sourceSchemaList);
        context.put("targetSchemaList", targetSchemaList);
        context.put("sourceSchema", sourceSchema);
        context.put("targetSchema", targetSchema);
        context.put("paginator", paginator);
        context.put("searchKey", searchKey);
        context.put("pageIndex", pageIndex);


    }


}
