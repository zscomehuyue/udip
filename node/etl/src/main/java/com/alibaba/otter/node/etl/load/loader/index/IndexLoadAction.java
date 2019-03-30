package com.alibaba.otter.node.etl.load.loader.index;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.event.IndexEvent;
import com.alibaba.otter.common.push.index.wide.event.IndexEventHandle;
import com.alibaba.otter.common.push.index.wide.ILoadIndexService;
import com.alibaba.otter.node.etl.common.index.service.IndexServiceFactory;
import com.alibaba.otter.node.etl.load.exception.IndexLoadException;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadAction;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.node.etl.load.loader.index.context.IndexLoadContext;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.LoadRoute;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.Identity;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

public class IndexLoadAction extends AbstractLoadAction {

    private IndexServiceFactory indexServiceFactory;

    private boolean pushIndexEvent = false;

    public IndexLoadAction() {
        super(DataMediaType.INDEX, "IndexLoadAction");
    }

    protected AbstractLoadContext buildContext(Identity identity) {
        IndexLoadContext context = new IndexLoadContext();
        context.setIdentity(identity);
        Channel channel = configClientService.findChannel(identity.getChannelId());
        Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
        context.setChannel(channel);
        context.setPipeline(pipeline);
        context.setDefaultLoadIndexService(indexServiceFactory.getLoadIndexService());
        return context;
    }

    @Override
    protected void doWorker(AbstractLoadContext context, boolean canBatch, List<Future<Exception>> results, List<EventData> rows, Long weight, OperateType type) {
        results.add(executor.submit(new IndexLoadWorker(context, rows, canBatch, weight, type)));
    }

    @Override
    protected void repareWorker(AbstractLoadContext context, List<EventData> retryEventDatas, boolean canBath, Long weight, OperateType type) {
        IndexLoadWorker worker = new IndexLoadWorker(context, retryEventDatas, canBath, weight, type);
        try {
            Exception ex = worker.call();
            if (ex != null) {
                throw ex;
            }
        } catch (Exception e) {
            log(ERROR, logger, () -> format("##service load phase two failed! ,error : %s", e));
            throw new IndexLoadException(e);
        }
    }

    @Override
    protected void repareWorker(AbstractLoadContext context, EventData eventData, boolean canBath, Long weight, OperateType type) {
        IndexLoadWorker worker = new IndexLoadWorker(context, Arrays.asList(eventData), false, weight, type);// 强制设置batch为false
        try {
            Exception ex = worker.call();
            if (ex != null) {
                log(WARN, logger, () -> String.format("service laod skip exception for data : %s , caused by %s", eventData.toString(), ExceptionUtils.getFullStackTrace(ex)));
            }
        } catch (Exception ex) {
            log(WARN, logger, () -> String.format("service laod skip exception for data : %s , caused by %s", eventData.toString(), ExceptionUtils.getFullStackTrace(ex)));
        }
    }


    class IndexLoadWorker implements Callable<Exception> {
        private AbstractLoadContext context;
        private ILoadIndexService service;
        private List<EventData> datas;
        private OperateType eventType;
        private boolean canBatch;
        private List<EventData> allFailedDatas = new ArrayList<EventData>();
        private List<EventData> allProcesedDatas = new ArrayList<EventData>();
        private List<EventData> processedDatas = new ArrayList<EventData>();
        private List<EventData> failedDatas = new ArrayList<EventData>();
        private DataMedia indexDataMedia;
        private DataMedia mysqlDataMedia;
        private LoadRoute loadRoute;
        private Long tableId;
        private Long weight;

        public IndexLoadWorker(AbstractLoadContext context, List<EventData> datas, boolean canBatch, Long weight, OperateType type) {
            this.context = context;
            this.datas = datas;
            this.canBatch = canBatch;
            this.eventType = type;
            this.weight = weight;
            EventData data = datas.get(0);
            this.tableId = data.getTableId();

            // eventData为同一数据库的记录，只取第一条即可 one pipeline have more wideIndex but slave is one ,
            indexDataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId(), DataMediaType.INDEX);
            mysqlDataMedia = ConfigHelper.findSourceDataMedia(context.getPipeline(), data.getTableId());
            loadRoute = ConfigHelper.findLoadRoute(context.getPipeline(), data.getTableId(), DataMediaType.INDEX);
            this.service = indexServiceFactory.getLoadIndexService(context.getIdentity().getPipelineId(), indexDataMedia);
        }

        public Exception call() throws Exception {
            try {
                Thread.currentThread().setName(String.format(WORKER_NAME_FORMAT, context.getPipeline().getId(), context.getPipeline().getName()));
                return doCall();
            } finally {
                Thread.currentThread().setName(workerName);
            }
        }

        private Exception doCall() {
            RuntimeException error = null;
            ExecuteResult exeResult = null;
            int index = 0;

            // 记录下处理成功的记录下标
            for (; index < datas.size(); ) {

                // 处理数据切分
                final List<EventData> splitDatas = new ArrayList<EventData>();
                if (canBatch) {
                    int end = (index + batchSize > datas.size()) ? datas.size() : (index + batchSize);
                    splitDatas.addAll(datas.subList(index, end));
                    index = end;
                } else {
                    splitDatas.add(datas.get(index));
                    index = index + 1;
                }

                int retryCount = 0;
                while (true) {
                    try {
                        if (!CollectionUtils.isEmpty(failedDatas)) {
                            splitDatas.clear();
                            splitDatas.addAll(failedDatas); // 下次重试时，只处理错误的记录
                        } else {
                            failedDatas.addAll(splitDatas); // 先添加为出错记录，可能获取lob,datasource会出错
                        }
                        failedDatas.clear(); // 先清理
                        processedDatas.clear();
                        doLoadAction(splitDatas);
                        error = null;
                        exeResult = ExecuteResult.SUCCESS;
                    } catch (DeadlockLoserDataAccessException ex) {
                        error = new IndexLoadException(ExceptionUtils.getFullStackTrace(ex),
                                IndexLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.RETRY;
                    } catch (DataIntegrityViolationException ex) {
                        error = new IndexLoadException(ExceptionUtils.getFullStackTrace(ex),
                                IndexLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    } catch (RuntimeException ex) {
                        error = new IndexLoadException(ExceptionUtils.getFullStackTrace(ex),
                                IndexLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    } catch (Throwable ex) {
                        error = new IndexLoadException(ExceptionUtils.getFullStackTrace(ex),
                                IndexLoadDumper.dumpEventDatas(splitDatas));
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
                        failedDatas.addAll(splitDatas);
                        if (retryCount >= retry) {
                            processFailedDatas(index);// 重试已结束，添加出错记录并退出
                            throw new IndexLoadException(String.format("service load execute [%s] retry %s times failed",
                                    context.getIdentity().toString(),
                                    retryCount), error);
                        } else {
                            try {
                                int wait = retryCount * retryWait;
                                wait = (wait < retryWait) ? retryWait : wait;
                                Thread.sleep(wait);
                            } catch (InterruptedException ex) {
                                Thread.interrupted();
                                processFailedDatas(index);// 局部处理出错了
                                throw new IndexLoadException(ex);
                            }
                        }
                    } else {

                        // 出现异常，理论上当前的批次都会失败
                        processedDatas.clear();
                        failedDatas.clear();
                        failedDatas.addAll(splitDatas);
                        processFailedDatas(index);// 局部处理出错了
                        throw error;
                    }
                }
            }


            // 记录一下当前处理过程中失败的记录,affect = 0的记录
            context.getFailedDatas().addAll(allFailedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);
            return null;
        }

        private void doLoadAction(List<EventData> splitDatas) {
            long start = System.currentTimeMillis();
            splitDatas.forEach(eventData -> {
                log(INFO, logger, () -> "=doLoadAction=>before add es ,table name:%s ,index:%s ,eventType:%s ,id:%s ,spend:%s milliseconds , batch size:%s, execTime:%s, execTimes:%s ", () -> {
                    return new Object[]{eventData.getTableName(), indexDataMedia.getNamespace()
                            , eventData.getEventType().getValue()
                            , eventData.getKeys().get(0).getColumnValue()
                            , String.valueOf(System.currentTimeMillis() - eventData.getExecuteTime())
                            , String.valueOf(splitDatas.size())
                            , eventData.getExecuteTime()
                            , LocalDateTime.ofInstant(Instant.ofEpochMilli(eventData.getExecuteTime())
                            , ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))};
                });
            });
            int[] signleTableAffects = new int[0];
            if (loadRoute.getType().isLoadSingleIndex()) {
                signleTableAffects = service.loadSingleIndex(indexDataMedia, splitDatas, eventType);
                processStat(splitDatas, start, signleTableAffects);
            }
            int[] wideTableAffects = service.loadWideIndex(indexDataMedia, splitDatas, eventType, tableId, context.getPipeline());
            service.publish(new IndexEvent(splitDatas, eventType, mysqlDataMedia, indexDataMedia, IndexEventHandle.class));
            if (signleTableAffects.length <= 0 && wideTableAffects.length >= splitDatas.size()) {
                processStat(splitDatas, start, wideTableAffects);
            }
        }

        private void processStat(List<EventData> splitDatas, long start, int[] affects) {
            try {
                for (int i = 0; i < splitDatas.size(); i++) {
                    EventData data = splitDatas.get(i);
                    log(INFO, logger
                            , () -> "=processStat=>after add es ,table name:%s ,index:%s ,eventType:%s ,id:%s,spend:%s milliseconds ,es use:%s  milliseconds ,batch size:%s , execTime:%s, execTimes:%s  ."
                            , () -> {
                                return new Object[]{data.getTableName()
                                        , indexDataMedia.getNamespace()
                                        , data.getEventType().getValue()
                                        , data.getKeys().get(0).getColumnValue()
                                        , String.valueOf(System.currentTimeMillis() - data.getExecuteTime())
                                        , String.valueOf(System.currentTimeMillis() - start)
                                        , String.valueOf(splitDatas.size())
                                        , data.getExecuteTime()
                                        , LocalDateTime.ofInstant(Instant.ofEpochMilli(data.getExecuteTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))};
                            });
                    if (affects[i] < 1) {
                        failedDatas.add(data); // 记录到错误的临时队列，进行重试处理
                    } else {
                        processedDatas.add(data); // 记录到成功的临时队列，commit也可能会失败。所以这记录也可能需要进行重试
                        IndexLoadAction.this.processStat(data, context);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                log(WARN, logger, () -> "=processStat=>stat error:%s", e);
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
            allProcesedDatas.addAll(processedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);// 添加历史成功记录
        }

    }

    public void setPushIndexEvent(boolean pushIndexEvent) {
        this.pushIndexEvent = pushIndexEvent;
    }

    public void setIndexServiceFactory(IndexServiceFactory indexServiceFactory) {
        this.indexServiceFactory = indexServiceFactory;
    }


}
