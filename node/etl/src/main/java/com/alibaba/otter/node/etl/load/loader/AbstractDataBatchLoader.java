package com.alibaba.otter.node.etl.load.loader;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.etl.model.*;
import com.google.common.base.Function;
import com.google.common.collect.OtterMigrateMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AbstractDataBatchLoader implements OtterLoader<DbBatch, List<LoadContext>>, BeanFactoryAware {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected ExecutorService executorService;
    protected BeanFactory beanFactory;
    protected ConfigClientService configClientService;
    protected LoadInterceptor loadInterceptor;

    public List<LoadContext> load(DbBatch data) {
        final RowBatch rowBatch = data.getRowBatch();

        final FileBatch fileBatch = data.getFileBatch();
        boolean existFileBatch = (rowBatch != null && !CollectionUtils.isEmpty(fileBatch.getFiles()) && data.getRoot() != null);
        boolean existRowBatch = (rowBatch != null && !CollectionUtils.isEmpty(rowBatch.getDatas()));

        int count = 0;
        List<RowBatch> rowBatchs = null;
        if (existRowBatch) {
            rowBatchs = splitByDataMedia(rowBatch);

            // 根据介质内容进行分类合并，每个介质一个载入通道
            count += rowBatchs.size();
        }
        if (existFileBatch) {
            count += 1;
        }
        WeightController controller = new WeightController(count);
        List<Future<LoadContext>> futures = new ArrayList<Future<LoadContext>>();
        ExecutorCompletionService completionService = new ExecutorCompletionService(executorService);

        if (existFileBatch) {
            submitFileBatch(futures, completionService, fileBatch, data.getRoot(), controller);
        }
        if (existRowBatch) {
            submitRowBatch(futures, completionService, rowBatchs, controller);
        }

        // 先获取一下异步处理的结果，记录一下出错的index
        List<LoadContext> processedContexts = new ArrayList<LoadContext>();
        int index = 0;
        LoadException exception = null;
        while (index < futures.size()) {
            try {
                completionService.take().get();// 它也可能被打断
            } catch (InterruptedException e) {
                exception = new LoadException(e);
                break;
            } catch (ExecutionException e) {
                exception = new LoadException(e);
                break;
            }

            index++;
        }

        // 任何一个线程返回，出现了异常，就退出整个调度
        if (index < futures.size()) {// 小于代表有错误，需要对未完成的记录进行cancel操作，对已完成的结果进行收集，做重复录入过滤记录
            for (int errorIndex = 0; errorIndex < futures.size(); errorIndex++) {
                Future<LoadContext> future = futures.get(errorIndex);
                if (future.isDone()) {
                    try {
                        loadInterceptor.error(future.get());// 做一下出错处理，记录到store中
                    } catch (InterruptedException e) {
                        // ignore
                    } catch (ExecutionException e) {
                        // ignore
                    } catch (Exception e) {
                        logger.error("interceptor process error failed", e);
                    }

                } else {
                    future.cancel(true); // 对未完成的进行取消
                }
            }
        } else {
            for (int i = 0; i < futures.size(); i++) {// 收集一下正确处理完成的结果
                try {
                    processedContexts.add(futures.get(i).get());
                } catch (InterruptedException e) {
                    // ignore
                } catch (ExecutionException e) {
                    // ignore
                }
            }
        }

        if (exception != null) {
            throw exception;
        } else {
            return processedContexts;
        }
    }

    protected void submitFileBatch(List<Future<LoadContext>> futures, ExecutorCompletionService completionService,
                                   final FileBatch fileBatch, final File rootDir, final WeightController controller) {
    }

    protected abstract void submitRowBatch(List<Future<LoadContext>> futures, ExecutorCompletionService completionService,
                                           final List<RowBatch> rowBatchs, final WeightController controller);

    /**
     * 将rowBatch中的记录，按找载入的目标数据源进行分类
     */
    private List<RowBatch> splitByDataMedia(RowBatch rowBatch) {
        final Identity identity = rowBatch.getIdentity();
        Map<DataMediaSource, RowBatch> result = OtterMigrateMap.makeComputingMap(new Function<DataMediaSource, RowBatch>() {

            public RowBatch apply(DataMediaSource input) {
                RowBatch rowBatch = new RowBatch();
                rowBatch.setIdentity(identity);
                return rowBatch;
            }
        });

        for (EventData eventData : rowBatch.getDatas()) {
            // 获取介质信息
            DataMedia media = ConfigHelper.findDataMedia(configClientService.findPipeline(identity.getPipelineId()),
                    eventData.getTableId());
            result.get(media.getSource()).merge(eventData); // 归类
        }

        return new ArrayList<RowBatch>(result.values());
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setLoadInterceptor(LoadInterceptor loadInterceptor) {
        this.loadInterceptor = loadInterceptor;
    }
}