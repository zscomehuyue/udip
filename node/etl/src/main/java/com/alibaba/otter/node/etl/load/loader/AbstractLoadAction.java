package com.alibaba.otter.node.etl.load.loader;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadDumper;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadMerger;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.node.etl.load.loader.weight.WeightBuckets;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

public abstract class AbstractLoadAction implements InitializingBean, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static final int DEFAULT_POOL_SIZE = 5;
    protected String workerName = "AbstractLoadAction";
    protected String WORKER_NAME_FORMAT = "pipelineId = %s , pipelineName = %s , ";
    protected int poolSize = DEFAULT_POOL_SIZE;
    protected int retry = 3;
    protected int retryWait = 3000;
    protected LoadInterceptor interceptor;
    protected ExecutorService executor;
    protected ConfigClientService configClientService;
    protected int batchSize = 50;
    protected boolean useBatch = true;
    protected LoadStatsTracker loadStatsTracker;
    protected DataMediaType loadType;

    public AbstractLoadAction(DataMediaType loadType, String workerName) {
        this.loadType = loadType;
        this.workerName = workerName;
        this.WORKER_NAME_FORMAT = WORKER_NAME_FORMAT + workerName;
    }

    protected Pipeline getPipeline(Long pipelineId) {
        return configClientService.findPipeline(pipelineId);
    }

    /**
     * 返回结果为已处理成功的记录
     */
    public AbstractLoadContext load(RowBatch rowBatch, WeightController controller) {
        Assert.notNull(rowBatch);
        Identity identity = rowBatch.getIdentity();

        AbstractLoadContext context = buildContext(identity);
        try {
            List<EventData> datas = rowBatch.getDatas();
            context.setPrepareDatas(datas);
            // 执行重复录入数据过滤
            datas = context.getPrepareDatas();
            if (datas == null || datas.size() == 0) {
                LogUtils.log(INFO, logger, () -> "##no eventdata for load, return");
                return context;
            }

            // 因为所有的数据在DbBatchLoader已按照DateMediaSource进行归好类，不同数据源介质会有不同的DbLoadAction进行处理
            // 设置media source时，只需要取第一节点的source即可
            setContext(context, datas);
            interceptor.prepare(context);
            // 执行重复录入数据过滤
            datas = context.getPrepareDatas();
            // 处理下ddl语句，ddl/dml语句不可能是在同一个batch中，由canal进行控制
            // 主要考虑ddl的幂等性问题，尽可能一个ddl一个batch，失败或者回滚都只针对这条sql
            if (isDdlDatas(datas)) {
                doDdl(context, datas);
            } else {
                WeightBuckets<EventData> buckets = buildWeightBuckets(context, datas);
                List<Long> weights = buckets.weights();
                controller.start(weights);// weights可能为空，也得调用start方法
                if (CollectionUtils.isEmpty(datas)) {
                    LogUtils.log(INFO, logger, () -> "##no eventdata for load");
                }
                adjustPoolSize(context); // 根据manager配置调整线程池
                adjustConfig(context); // 调整一下运行参数
                // 按权重构建数据对象
                // 处理数据
                for (int i = 0; i < weights.size(); i++) {
                    Long weight = weights.get(i);
                    controller.await(weight.intValue());
                    // 处理同一个weight下的数据
                    List<EventData> items = buckets.getItems(weight);
                    LogUtils.log(DEBUG, logger, () -> "##start load for weight:" + weight);
                    // 预处理下数据

                    // 进行一次数据合并，合并相同pk的多次I/U/D操作
                    items = DbLoadMerger.merge(items);
                    // 按I/U/D进行归并处理
                    LoadData loadData = new LoadData();
                    doBefore(items, context, loadData);
                    // 执行load操作
                    doLoad(context, loadData, weight);
                    controller.single(weight.intValue());
                    LogUtils.log(DEBUG, logger, () -> "##end load for weight:" + weight);
                }
            }
            interceptor.commit(context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            interceptor.error(context);
        } catch (Exception e) {
            interceptor.error(context);
            throw new LoadException(e);
        }

        return context;// 返回处理成功的记录
    }

    protected abstract AbstractLoadContext buildContext(Identity identity);

    protected void setContext(AbstractLoadContext context, List<EventData> datas) {

    }

    /**
     * 分析整个数据，将datas划分为多个批次. ddl sql前的DML并发执行，然后串行执行ddl后，再并发执行DML
     *
     * @return
     */
    protected boolean isDdlDatas(List<EventData> eventDatas) {
        boolean result = false;
        for (EventData eventData : eventDatas) {
            result |= eventData.getEventType().isDdl();
            if (result && !eventData.getEventType().isDdl()) {
                throw new LoadException("ddl/dml can't be in one batch, it's may be a bug , pls submit issues.",
                        DbLoadDumper.dumpEventDatas(eventDatas));
            }
        }
        return result;
    }

    /**
     * 构建基于weight权重分组的item集合列表
     */
    protected WeightBuckets<EventData> buildWeightBuckets(AbstractLoadContext context, List<EventData> datas) {
        WeightBuckets<EventData> buckets = new WeightBuckets<EventData>();
        for (EventData data : datas) {
            // 获取对应的weight
            DataMediaPair pair = ConfigHelper.findDataMediaPair(context.getPipeline(), data.getPairId());
            buckets.addItem(pair.getPushWeight(), data);
        }

        return buckets;
    }

    /**
     * 执行数据处理，比如数据冲突检测
     */
    protected void doBefore(List<EventData> items, final AbstractLoadContext context, final LoadData loadData) {
        for (final EventData item : items) {
            boolean filter = interceptor.before(context, item);
            if (!filter) {
                loadData.merge(item);// 进行分类
            }
        }
    }


    protected void doLoad(final AbstractLoadContext context, LoadData loadData, Long weight) {
        // 优先处理delete,可以利用batch优化
        List<List<EventData>> batchDatas = new ArrayList<List<EventData>>();
        for (LoadData.TableLoadData tableData : loadData.getTables()) {
            if (ConfigHelper.needLoadMedia(context.getPipeline(), tableData.getTableId(), loadType)) {
                if (useBatch) {

                    // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
                    batchDatas.addAll(split(tableData.getDeleteDatas()));
                } else {

                    // 如果不可以执行batch，则按照单条数据进行并行提交
                    // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
                    for (EventData data : tableData.getDeleteDatas()) {
                        batchDatas.add(Arrays.asList(data));
                    }
                }
            }
        }
        if (!ConfigHelper.needLoadMedia(context.getPipeline(), loadType)) {
            return;
        }
        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {

            //FIXME 处理deletedata
            doTwoPhase(context, batchDatas, true, weight, OperateType.DELETE);
        }
        batchDatas.clear();

        //FIXME 处理下insert
        for (LoadData.TableLoadData tableData : loadData.getTables()) {
            if (ConfigHelper.needLoadMedia(context.getPipeline(), tableData.getTableId(), loadType)) {
                if (useBatch) {

                    // 执行insert + update语句
                    batchDatas.addAll(split(tableData.getInsertDatas()));
                } else {

                    // 执行insert + update语句
                    for (EventData data : tableData.getInsertDatas()) {
                        batchDatas.add(Arrays.asList(data));
                    }
                }
            }
        }

        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {
            doTwoPhase(context, batchDatas, true, weight, OperateType.INSERT);
        }
        batchDatas.clear();


        //FIXME 处理下update
        for (LoadData.TableLoadData tableData : loadData.getTables()) {
            if (ConfigHelper.needLoadMedia(context.getPipeline(), tableData.getTableId(), loadType)) {
                if (useBatch) {
                    batchDatas.addAll(split(tableData.getUpadateDatas()));// 每条记录分为一组，并行加载
                } else {
                    for (EventData data : tableData.getUpadateDatas()) {
                        batchDatas.add(Arrays.asList(data));
                    }
                }
            }
        }
        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {
            doTwoPhase(context, batchDatas, true, weight, OperateType.UPDATE);
        }
        batchDatas.clear();

    }

    /**
     * 将对应的数据按照sql相同进行batch组合
     */
    private List<List<EventData>> split(List<EventData> datas) {
        List<List<EventData>> result = new ArrayList<List<EventData>>();
        if (datas == null || datas.size() == 0) {
            return result;
        } else {
            int[] bits = new int[datas.size()];// 初始化一个标记，用于标明对应的记录是否已分入某个batch
            for (int i = 0; i < bits.length; i++) {
                // 跳过已经被分入batch的
                while (i < bits.length && bits[i] == 1) {
                    i++;
                }

                if (i >= bits.length) { // 已处理完成，退出
                    break;
                }

                // 开始添加batch，最大只加入batchSize个数的对象
                List<EventData> batch = new ArrayList<EventData>();
                bits[i] = 1;
                batch.add(datas.get(i));
                for (int j = i + 1; j < bits.length && batch.size() < batchSize; j++) {
                    if (bits[j] == 0 && canBatch(datas.get(i), datas.get(j))) {
                        batch.add(datas.get(j));
                        bits[j] = 1;// 修改为已加入
                    }
                }
                result.add(batch);
            }

            return result;
        }
    }

    /**
     * 判断两条记录是否可以作为一个batch提交，主要判断sql是否相等. 可优先通过schemaName进行判断
     */
    private boolean canBatch(EventData source, EventData target) {
        return source.getTableName().equals(target.getTableName());
    }


    private void doDryRun(AbstractLoadContext context, List<List<EventData>> totalRows, boolean canBatch) {
        for (List<EventData> rows : totalRows) {
            if (CollectionUtils.isEmpty(rows)) {
                continue; // 过滤空记录
            }

            for (EventData row : rows) {
                processStat(row, context);// 直接记录成功状态
            }

            context.getProcessedDatas().addAll(rows);
        }
    }

    /**
     * 执行ddl的调用，处理逻辑比较简单: 串行调用
     *
     * @param context
     * @param eventDatas
     */
    protected void doDdl(AbstractLoadContext context, List<EventData> eventDatas) {
        eventDatas.forEach(eventData -> {
            context.getProcessedDatas().add(eventData);
        });
    }

    /**
     * 首先进行并行执行，出错后转为串行执行
     */
    protected void doTwoPhase(AbstractLoadContext context, List<List<EventData>> totalRows, boolean canBatch, Long weight, OperateType type) {
        // 预处理下数据
        List<Future<Exception>> results = new ArrayList<Future<Exception>>();
        for (List<EventData> rows : totalRows) {
            if (CollectionUtils.isEmpty(rows)) {
                continue; // 过滤空记录
            }
            doWorker(context, canBatch, results, rows, weight, type);
        }

        boolean partFailed = false;
        for (int i = 0; i < results.size(); i++) {
            Future<Exception> result = results.get(i);
            Exception ex = null;
            try {
                ex = result.get();
                for (EventData data : totalRows.get(i)) {
                    interceptor.after(context, data);// 通知加载完成
                }
            } catch (Exception e) {
                ex = e;
            }
            if (ex != null) {
                LogUtils.log(WARN, logger, () -> "##load phase one failed! error:%s", ex);
                partFailed = true;
            }
        }
        if (partFailed) {

            // 尝试的内容换成phase one跑的所有数据，避免因failed datas计算错误而导致丢数据
            List<EventData> retryEventDatas = new ArrayList<EventData>();
            for (List<EventData> rows : totalRows) {
                retryEventDatas.addAll(rows);
            }

            context.getFailedDatas().clear(); // 清理failed data数据

            // 可能为null，manager老版本数据序列化传输时，因为数据库中没有skipLoadException变量配置
            Boolean skipLoadException = context.getPipeline().getParameters().getSkipLoadException();
            if (skipLoadException != null && skipLoadException) {// 如果设置为允许跳过单条异常，则一条条执行数据load，准确过滤掉出错的记录，并进行日志记录
                for (EventData retryEventData : retryEventDatas) {
                    repareWorker(context, retryEventData, false, weight, type);
                }
            } else {
                repareWorker(context, retryEventDatas, true, weight, type);
            }

            // 清理failed data数据
            for (EventData data : retryEventDatas) {
                interceptor.after(context, data);// 通知加载完成
            }
        }

    }

    protected abstract void doWorker(AbstractLoadContext context, boolean canBatch, List<Future<Exception>> results, List<EventData> rows, Long weight, OperateType type);

    protected abstract void repareWorker(AbstractLoadContext context, List<EventData> retryEventDatas, boolean canBath, Long weight, OperateType type);

    protected abstract void repareWorker(AbstractLoadContext context, EventData eventData, boolean canBath, Long weight, OperateType type);

    // 调整一下线程池
    protected void adjustPoolSize(AbstractLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        int newPoolSize = pipeline.getParameters().getLoadPoolSize();
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }
    }

    protected void adjustConfig(AbstractLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        this.useBatch = pipeline.getParameters().isUseBatch();
    }

    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(poolSize * 4),
                new NamedThreadFactory(workerName),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    protected void processStat(EventData data, AbstractLoadContext context) {
        LoadStatsTracker.LoadThroughput throughput = loadStatsTracker.getStat(context.getIdentity());
        LoadStatsTracker.LoadCounter counter = throughput.getStat(data.getPairId());
        EventType type = data.getEventType();
        if (type.isInsert()) {
            counter.getInsertCount().incrementAndGet();
        } else if (type.isUpdate()) {
            counter.getUpdateCount().incrementAndGet();
        } else if (type.isDelete()) {
            counter.getDeleteCount().incrementAndGet();
        }
        counter.getRowCount().incrementAndGet();
        counter.getRowSize().addAndGet(data.getSize());
    }


    public enum ExecuteResult {
        SUCCESS, ERROR, RETRY
    }

    // =============== setter / getter ===============

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setRetryWait(int retryWait) {
        this.retryWait = retryWait;
    }

    public void setInterceptor(LoadInterceptor interceptor) {
        this.interceptor = interceptor;
    }


    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setLoadStatsTracker(LoadStatsTracker loadStatsTracker) {
        this.loadStatsTracker = loadStatsTracker;
    }

    public void setUseBatch(boolean useBatch) {
        this.useBatch = useBatch;
    }

}
