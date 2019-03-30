package com.alibaba.otter.manager.biz.check;

import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.hwl.otter.clazz.repairlog.dal.dataobject.CheckRepairLogDo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @Description: 修复功能线程
 * @Author: tangdelong
 * @Date: 2018/6/22 16:53
 */
public class RepairThread implements Callable<String[]> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private CheckService checkService;

    private DataMediaPair dataMediaPair;

    private String beginTime;

    private String endTime;

    private Pipeline pipeline;

    private OperateEnum type = OperateEnum.Repair;

    public RepairThread(String beginTime, String endTime, Pipeline pipeline, DataMediaPair dataMediaPair, CheckService checkService) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.dataMediaPair = dataMediaPair;
        this.checkService = checkService;
        this.pipeline = pipeline;
    }

    public RepairThread(String beginTime, String endTime, Pipeline pipeline, DataMediaPair dataMediaPair, CheckService checkService, OperateEnum type) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.dataMediaPair = dataMediaPair;
        this.checkService = checkService;
        this.pipeline = pipeline;
        this.type = type;
    }


    /**
     * @return [0]:修复是否成功0：成功,[1]:消息信息
     * @throws Exception
     */
    @Override
    public String[] call() throws Exception {
        String[] result = new String[3];
        switch (type) {
            case Repair:
                result = repairData(true);
                break;
            case forceRepair:
                result = repairData(false);
                break;
            case findAndRepair:
                result = subRepairData();
                break;
            case unknown:
                result[0] = "1";
                result[1] = "do anything";
                result[2] = "do anything";
                break;
            case checkFieldAndRepair:
                result = checkSpecialField();
                break;
            case timer:
                result = repairData(true);
                checkSpecialField();
                break;
            default:
                result = repairData(true);
                break;

        }
        return result;
    }

    /**
     * simple check special field ;
     */
    private String[] checkSpecialField() {
        String[] result = new String[3];
        result[0] = "1";
        result[1] = "";
        result[2] = pipeline.getName();
        checkService.checkDirtyData(beginTime, endTime, dataMediaPair, pipeline);
        return result;
    }

    private String[] subRepairData() {
        return checkService.addOmitDatas(beginTime, endTime, dataMediaPair, pipeline);
    }

    @NotNull
    private String[] repairData(boolean checkSum) {
        String[] result = new String[3];
        try {
            int repairFailNum = 0;
            CheckRepairLogDo checkRepairLog = checkService.getCheckRepairLog(dataMediaPair, beginTime, endTime);
            if (checkRepairLog != null) {
                repairFailNum = checkRepairLog.getRepairNum();
                repairFailNum += 1;
            } else {
                repairFailNum += 1;
            }

            // 数据对比
            Integer[] count = checkService.getSourceDataCountTargetDataCount(beginTime, endTime, dataMediaPair, pipeline);
            boolean compare = checkService.sourceDataCompareTargetData(count[0], count[1]);
            if (checkSum && compare) {
                result[0] = compare ? "0" : "1";
                return result;
            }

            log.info("==================================================");
            log.info("数据修复操作，PipelineId：" + dataMediaPair.getPipelineId() + "，源表名称：" + dataMediaPair.getSource().getNamespace() + "." + dataMediaPair.getSource().getName() +
                    "目标表名称：" + dataMediaPair.getTarget().getNamespace() + "." + dataMediaPair.getTarget().getName() + ",   beginTime:" + beginTime + ",  endTime:" + endTime);
            log.info("==================================================");

            // 发送通知
            checkService.sendRepairWarningMessage(dataMediaPair.getPipelineId(), dataMediaPair, beginTime, endTime, count);

            // 数据修复
            int insertCount = checkService.insertRetl(beginTime, endTime, dataMediaPair, count[0]);
            if (insertCount > 0) {
                // 给自由门一定的同步时间
                Thread.sleep(20 * 1000);

                // 检查修复是否成功
                boolean compare1 = checkService.sourceDataCompareTargetData(beginTime, endTime, dataMediaPair, pipeline);
                result[0] = compare1 ? "0" : "1";
                if (compare1) {
                    checkService.insertRepairLog(pipeline.getChannelId(), dataMediaPair, beginTime, endTime, repairFailNum, 0); // 记录成功修复日志
                    // 更改同条件下的状态为成功
                    checkService.updateCheckRepairLogDoState(dataMediaPair, beginTime, endTime, 0);
                } else {
                    checkService.insertRepairLog(pipeline.getChannelId(), dataMediaPair, beginTime, endTime, repairFailNum, 1); // 记录失败修复日志
                    log.error("数据修复失败,请尝试手动触发修复！");
                }
            } else {
                log.error("数据修复失败！");
                result[1] = "数据修复失败！";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result[1] = "数据修复失败，msg:" + e.getMessage();
        }
        result[2] = pipeline.getName();
        return result;
    }

}
