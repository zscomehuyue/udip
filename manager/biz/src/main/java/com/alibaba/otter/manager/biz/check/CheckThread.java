package com.alibaba.otter.manager.biz.check;

import com.alibaba.otter.manager.biz.check.exception.NotUseException;
import com.alibaba.otter.manager.biz.check.thread.ExecutorManager;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * @Description: 检查源与目标表数据
 * @Author: tangdelong
 * @Date: 2018/6/21 20:06
 */
public class CheckThread implements Callable<String[]> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private CheckService checkService;

    private DataMediaPair dataMediaPair;

    private Pipeline pipeline;

    private String beginDate;

    private String endDate;

    // true:指定开始和结束时间
    private boolean assginDate = false;

    private OperateEnum type = OperateEnum.Repair;

    public CheckThread(CheckService checkService, DataMediaPair dataMediaPair, Pipeline pipeline) {
        this.checkService = checkService;
        this.dataMediaPair = dataMediaPair;
        this.pipeline = pipeline;
    }

    public CheckThread(CheckService checkService, DataMediaPair dataMediaPair, Pipeline pipeline, OperateEnum type) {
        this.checkService = checkService;
        this.dataMediaPair = dataMediaPair;
        this.pipeline = pipeline;
        this.type = type;
    }

    public CheckThread(CheckService checkService, DataMediaPair dataMediaPair, Pipeline pipeline,
                       boolean assginDate, String beginDate, String endDate) {
        this.checkService = checkService;
        this.dataMediaPair = dataMediaPair;
        this.pipeline = pipeline;
        this.assginDate = assginDate;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }


    /**
     * @return [0]:源数据量，[1]:目标数据量，[2]：源和目标对比0(成功)和1(失败),[3]:消息信息
     * @throws Exception
     */
    @Override
    public String[] call() throws Exception {
        String[] result = new String[5];
        try {

            //check_data_info
            DataCheckDo dc = null;
            String beginTime = null;
            String endTime = null;
            if (assginDate) { // 指定时间段检查，只检查不修复
                beginTime = beginDate;
                endTime = endDate;
            } else {

                //  数据检查对象获取
                dc = checkService.getDataCheckDo(dataMediaPair);

                // 当前时间前几分钟
                String[] time = checkService.getBeginEndTime(dc);
                beginTime = time[0];
                endTime = time[1];
            }

            // 数据同步对比
            Integer[] count = checkService.getSourceDataCountTargetDataCount(beginTime, endTime, dataMediaPair, pipeline);
            boolean compare = checkService.sourceDataCompareTargetData(count[0], count[1]);
            result[0] = String.valueOf(count[0]);
            result[1] = String.valueOf(count[1]);
            result[2] = String.valueOf(compare ? 0 : 1);
            if (assginDate) {// 指定时间段检查，只检查不修复

                // 修复日志状态更新
                if (compare) {
                    checkService.updateCheckRepairLogDoState(dataMediaPair, beginTime, endTime, 0);
                }
            } else {

                // 数据同步时间点位更新
                dc = checkService.updateDataCheckDo(dc, pipeline.getChannelId(), dataMediaPair, beginTime, endTime);
                if (dc.getIsStart() == 0) { //开启状态
                    // 检查失败后进行修复操作
                    if (!compare) {
                        ExecutorManager.submit(new RepairThread(beginTime, endTime, pipeline, dataMediaPair, checkService, type));
                    }
                }
            }
            log.info("==================================================");
            log.info("数据检查操作，PipelineId：" + dataMediaPair.getPipelineId() + "，源表名称：" + dataMediaPair.getSource().getNamespace() + "." + dataMediaPair.getSource().getName() +
                    "目标表名称：" + dataMediaPair.getTarget().getNamespace() + "." + dataMediaPair.getTarget().getName() + ",   beginTime:" + beginTime + ",  endTime:" + endTime);
            log.info("==================================================");
        } catch (NotUseException e) {
            log.info(e.getMessage());
            result[3] = e.getMessage();
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result[3] = e.getMessage();
            return result;
        }
        result[4] = pipeline.getName();
        return result;
    }


}
