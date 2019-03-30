package com.alibaba.otter.node.etl.load.loader.mq;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.node.etl.common.mq.IEventDataMqService;
import com.alibaba.otter.node.etl.common.mq.service.MqServiceFactory;
import com.alibaba.otter.node.etl.load.exception.MqLoadException;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadAction;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.node.etl.load.loader.mq.context.MqLoadContext;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.Identity;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class MqLoadAction extends AbstractLoadAction {

    private MqServiceFactory mqServiceFactory;

    public MqLoadAction() {
        super(DataMediaType.MQ, "MqLoadAction");
    }

    @Override
    protected AbstractLoadContext buildContext(Identity identity) {
        MqLoadContext context = new MqLoadContext();
        context.setIdentity(identity);
        Channel channel = configClientService.findChannel(identity.getChannelId());
        Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
        context.setChannel(channel);
        context.setPipeline(pipeline);
        context.setDefaultMqService(mqServiceFactory.getDefaultRabbitService());
        return null;
    }

    @Override
    protected void doWorker(AbstractLoadContext context, boolean canBatch, List<Future<Exception>> results, List<EventData> rows, Long weight, OperateType type) {
        results.add(executor.submit(new MqLoadWorker(context, rows)));
    }

    @Override
    protected void repareWorker(AbstractLoadContext context, List<EventData> retryEventDatas, boolean canBath, Long weight, OperateType type) {
        MqLoadWorker worker = new MqLoadWorker(context, retryEventDatas);
        try {
            Exception ex = worker.call();
            if (ex != null) {
                throw ex;
            }
        } catch (Exception e) {
            logger.error("##mq load phase two failed!", e);
            throw new MqLoadException(e);
        }
    }

    @Override
    protected void repareWorker(AbstractLoadContext context, EventData eventData, boolean canBath, Long weight, OperateType type) {
        MqLoadWorker worker = new MqLoadWorker(context, Arrays.asList(eventData));
        try {
            Exception ex = worker.call();
            if (ex != null) {
                logger.warn("mq laod skip exception for data : {} , caused by {}", eventData, ExceptionUtils.getFullStackTrace(ex));
            }
        } catch (Exception ex) {
            logger.warn("mq laod skip exception for data : {} , caused by {}", eventData, ExceptionUtils.getFullStackTrace(ex));
        }
    }


    class MqLoadWorker implements Callable<Exception> {
        private AbstractLoadContext context;
        private IEventDataMqService service;
        private List<EventData> datas;
        private List<EventData> allFailedDatas = new ArrayList<EventData>();
        private List<EventData> allProcesedDatas = new ArrayList<EventData>();
        private List<EventData> processedDatas = new ArrayList<EventData>();
        private List<EventData> failedDatas = new ArrayList<EventData>();
        private DataMedia mqDataMedia;

        public MqLoadWorker(AbstractLoadContext context, List<EventData> datas) {
            this.context = context;
            this.datas = datas;
            EventData data = datas.get(0);
            mqDataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId(), DataMediaType.ES);
            this.service = mqServiceFactory.getMqService(context.getIdentity().getPipelineId(), (MqMediaSource) mqDataMedia.getSource());
            if (null == this.service) {
                this.service = mqServiceFactory.getDefaultRabbitService();
            }
        }

        public Exception call() throws Exception {
            try {
                Thread.currentThread().setName(String.format(WORKER_NAME_FORMAT,
                        context.getPipeline().getId(),
                        context.getPipeline().getName()));
                return doCall();
            } finally {
                Thread.currentThread().setName(workerName);
            }
        }

        private Exception doCall() {
            RuntimeException error = null;
            ExecuteResult exeResult = null;
            for (int index = 0; index < datas.size(); index++) {
                int retryCount = 0;
                while (true) {
                    try {
                        failedDatas.add(datas.get(index)); // 先添加为出错记录，可能获取lob,datasource会出错
                        failedDatas.clear(); // 先清理
                        processedDatas.clear();
                        doLoadAction(datas.get(index));
                        error = null;
                        exeResult = ExecuteResult.SUCCESS;
                    } catch (DeadlockLoserDataAccessException ex) {
                        error = new MqLoadException(ExceptionUtils.getFullStackTrace(ex),
                                MqLoadDumper.dumpEventData(datas.get(index)));
                        exeResult = ExecuteResult.RETRY;
                    } catch (DataIntegrityViolationException ex) {
                        error = new MqLoadException(ExceptionUtils.getFullStackTrace(ex),
                                MqLoadDumper.dumpEventData(datas.get(index)));
                        exeResult = ExecuteResult.ERROR;
                    } catch (RuntimeException ex) {
                        error = new MqLoadException(ExceptionUtils.getFullStackTrace(ex),
                                MqLoadDumper.dumpEventData(datas.get(index)));
                        exeResult = ExecuteResult.ERROR;
                    } catch (Throwable ex) {
                        error = new MqLoadException(ExceptionUtils.getFullStackTrace(ex),
                                MqLoadDumper.dumpEventData(datas.get(index)));
                        exeResult = ExecuteResult.ERROR;
                    }

                    if (ExecuteResult.SUCCESS == exeResult) {
                        allFailedDatas.addAll(failedDatas);// 记录一下异常到all记录中
                        allProcesedDatas.addAll(processedDatas);
                        failedDatas.clear();// 清空上一轮的处理
                        processedDatas.clear();
                        break;
                    } else if (ExecuteResult.RETRY == exeResult) {
                        retryCount = retryCount + 1;// 计数一次

                        // 出现异常，理论上当前的批次都会失败
                        processedDatas.clear();
                        failedDatas.clear();
                        failedDatas.add(datas.get(index));
                        if (retryCount >= retry) {
                            processFailedDatas(index);// 重试已结束，添加出错记录并退出
                            throw new MqLoadException(String.format("mq load execute [%s] retry %s times failed",
                                    context.getIdentity().toString(),
                                    retryCount), error);
                        } else {
                            try {
                                int wait = retryCount * retryWait;
                                wait = (wait < retryWait) ? retryWait : wait;
                                Thread.sleep(wait);
                            } catch (InterruptedException ex) {
                                Thread.interrupted();
                                processFailedDatas(index);
                                throw new MqLoadException(ex);
                            }
                        }
                    } else {
                        processedDatas.clear();
                        failedDatas.clear();
                        failedDatas.add(datas.get(index));
                        processFailedDatas(index);
                        throw error;
                    }
                }
            }
            context.getFailedDatas().addAll(allFailedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);
            return null;
        }

        private void doLoadAction(EventData data) {
            service.sendMessage(mqDataMedia.getNamespace(), data);
        }

        private void processStat(EventData data, int affect, boolean batch) {
            if (batch && (affect < 1 && affect != Statement.SUCCESS_NO_INFO)) {
                failedDatas.add(data); // 记录到错误的临时队列，进行重试处理
            } else if (!batch && affect < 1) {
                failedDatas.add(data);// 记录到错误的临时队列，进行重试处理
            } else {
                processedDatas.add(data); // 记录到成功的临时队列，commit也可能会失败。所以这记录也可能需要进行重试
                MqLoadAction.this.processStat(data, context);
            }
        }

        // 出现异常回滚了，记录一下异常记录
        private void processFailedDatas(int index) {
            allFailedDatas.addAll(failedDatas);// 添加失败记录
            context.getFailedDatas().addAll(allFailedDatas);// 添加历史出错记录
            for (; index < datas.size(); index++) { // 记录一下未处理的数据
                context.getFailedDatas().add(datas.get(index));
            }
            // 这里不需要添加当前成功记录，出现异常后会rollback所有的成功记录，比如processDatas有记录，但在commit出现失败
            // (bugfix)
            allProcesedDatas.addAll(processedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);// 添加历史成功记录
        }

    }


    public void setMqServiceFactory(MqServiceFactory mqServiceFactory) {
        this.mqServiceFactory = mqServiceFactory;
    }

}
