package com.alibaba.otter.node.etl.load;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.common.jmx.StageAggregation;
import com.alibaba.otter.node.etl.common.pipe.PipeKey;
import com.alibaba.otter.node.etl.common.task.GlobalTask;
import com.alibaba.otter.node.etl.extract.SetlFuture;
import com.alibaba.otter.node.etl.load.loader.LoadContext;
import com.alibaba.otter.node.etl.load.loader.OtterLoaderFactory;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import com.alibaba.otter.shared.etl.model.DbBatch;
import org.slf4j.MDC;

import java.util.List;

public class MqLoadTask extends GlobalTask {
    private OtterLoaderFactory otterMqLoaderFactory;
    private LoadInterceptor mqLoadInterceptor;


    public MqLoadTask(Long pipelineId) {
        super(pipelineId);
    }


    public void run() {
        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
        while (running) {
            try {
                final EtlEventData etlEventData = arbitrateEventService.mqLoadEvent().await(pipelineId);
                Runnable task = new Runnable() {

                    public void run() {
                        // 设置profiling信息
                        boolean profiling = isProfiling();
                        Long profilingStartTime = null;
                        if (profiling) {
                            profilingStartTime = System.currentTimeMillis();
                        }
                        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(pipelineId));
                        String currentName = Thread.currentThread().getName();
                        Thread.currentThread().setName(createTaskName(pipelineId, "LoadWorker"));
                        List<LoadContext> processedContexts = null;
                        try {

                            // 后续可判断同步数据是否为rowData
                            List<PipeKey> keys = (List<PipeKey>) etlEventData.getDesc();
                            DbBatch dbBatch = rowDataPipeDelegate.get(keys);
                            if (dbBatch == null) {
                                processMissData(pipelineId, "load miss data with keys:" + keys.toString());
                                return;
                            }

                            // 进行数据load处理
                            otterMqLoaderFactory.setStartTime(dbBatch.getRowBatch().getIdentity(), etlEventData.getStartTime());
                            if (ConfigHelper.needLoadMedia(getPipeline(), DataMediaType.MQ)) {
                                processedContexts = otterMqLoaderFactory.load(dbBatch);
                            }
                            if (profiling) {
                                Long profilingEndTime = System.currentTimeMillis();
                                stageAggregationCollector.push(pipelineId, StageType.MQLOAD, new StageAggregation.AggregationItem(profilingStartTime, profilingEndTime));
                            }
                            // 处理完成后通知single已完成
                            arbitrateEventService.mqLoadEvent().single(etlEventData);
                        } catch (Throwable e) {
                            if (!isInterrupt(e)) {
                                logger.error(String.format("[%s] mq loadWork executor is error! data:%s", pipelineId, etlEventData), e);
                            } else {
                                logger.info(String.format("[%s] mq loadWork executor is interrrupt! data:%s", pipelineId, etlEventData), e);
                            }

                            if (processedContexts != null) {// 说明load成功了，但是通知仲裁器失败了，需要记录下记录到store
                                for (LoadContext context : processedContexts) {
                                    try {
                                        mqLoadInterceptor.error(context);
                                    } catch (Throwable ie) {
                                    }
                                }
                            }
                            if (!isInterrupt(e)) {
                                sendRollbackTermin(pipelineId, e);
                            }
                        } finally {
                            Thread.currentThread().setName(currentName);
                            MDC.remove(OtterConstants.splitPipelineLogFileKey);
                        }
                    }
                };

                // 构造pending任务，可在关闭线程时退出任务
                SetlFuture extractFuture = new SetlFuture(StageType.MQLOAD, etlEventData.getProcessId(), pendingFuture, task);
                executorService.execute(extractFuture);
            } catch (Throwable e) {
                if (isInterrupt(e)) {
                    logger.info(String.format("[%s] loadTask is interrupted!", pipelineId), e);
                    return;
                } else {
                    sendRollbackTermin(pipelineId, e); // 先解除lock，后发送rollback信号
                }
            }
        }
    }

    public void setOtterMqLoaderFactory(OtterLoaderFactory otterMqLoaderFactory) {
        this.otterMqLoaderFactory = otterMqLoaderFactory;
    }

    public void setMqLoadInterceptor(LoadInterceptor mqLoadInterceptor) {
        this.mqLoadInterceptor = mqLoadInterceptor;
    }
}
