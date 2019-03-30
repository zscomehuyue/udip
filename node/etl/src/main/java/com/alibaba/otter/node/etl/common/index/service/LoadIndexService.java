package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.IEventDataIndexService;
import com.alibaba.otter.common.push.index.wide.ILoadIndexService;
import com.alibaba.otter.common.push.index.wide.config.IndexConfigServiceFactory;
import com.alibaba.otter.common.push.index.wide.event.IndexEvent;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.load.exception.IndexLoadException;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.common.utils.TheadPoolUtils;
import com.alibaba.otter.shared.etl.model.EventData;
import org.elasticsearch.search.query.QueryPhaseExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

public class LoadIndexService implements ILoadIndexService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IEventDataIndexService eventDataIndexService;
    protected ConfigClientService configClientService;
    private IndexConfigServiceFactory indexConfigServiceFactory;

    /**
     * FieldMapping
     * fieldFormat
     * dataConvert
     * handleFieldType
     * FixField
     *
     * @param indexDataMedia
     * @param datas
     * @param eventType
     * @return
     */
    @Override
    public int[] loadSingleIndex(DataMedia indexDataMedia, List<EventData> datas, OperateType eventType) {
        int[] result;
        switch (eventType) {
            case DELETE:
                result = eventDataIndexService.batchDeleteByIds(indexDataMedia.getNamespace(), indexDataMedia.getName(), datas);
                break;
            case INSERT:
                result = eventDataIndexService.batchSingleSaveByIds(indexDataMedia.getNamespace(), indexDataMedia.getName(), datas);
                break;
            case UPDATE:
                result = eventDataIndexService.batchSingleUpdateByIds(indexDataMedia.getNamespace(), indexDataMedia.getName(), datas);
                break;
            default:
                log(WARN, logger, () -> "=loadSingleIndex=> operateType:%s", eventType.getName());
                result = datas.stream().mapToInt(value -> 1).toArray();
                break;
        }
        return result;
    }

    /**
     * 1.宽表不依附pipeline；但是子表依附pipeline；
     * 2.一个pipeline可以包含多个宽表；
     * 3.宽表脱离pipeline暂不考虑；
     */
    @Override
    public int[] loadWideIndex(DataMedia index, List<EventData> datas, OperateType eventType, Long tableId, Pipeline pipeline) {
        int[] result = new int[0];
        try {
            List<WideTable> tableIdWideList = configClientService.findWideTable(0l, tableId);
            if (CollectionUtils.isEmpty(tableIdWideList)) {
                return result;
            }
            boolean isMaster = isMasterTableId(tableId, tableIdWideList);
            switch (eventType) {
                case DELETE:
                    if (isMaster) {
                        result = eventDataIndexService.batchDeleteByIds(index.getNamespace(), index.getName(), datas);
                    }//不处理宽表中，删除部分子表的情况；
                    break;
                case INSERT:
                    if (isMaster) {
                        List<WideTable> wideList = tableIdWideList.stream().filter(wideTable -> wideTable.getTarget().getId().equals(index.getId())).collect(Collectors.toList());
                        result = eventDataIndexService.batchWideSaveByIds(index.getNamespace(), index.getName(), datas, wideList, pipeline);
                    } else {
                        result = handleWideIndexUpdateBySlaves(datas, pipeline, tableIdWideList, OperateType.INSERT);
                    }
                    break;
                case UPDATE:
                    if (isMaster) {
                        List<WideTable> wideList = tableIdWideList.stream().filter(wideTable -> wideTable.getTarget().getId().equals(index.getId())).collect(Collectors.toList());
                        result = eventDataIndexService.batchWideUpdateByIds(index.getNamespace(), index.getName(), datas, wideList, pipeline);
                    } else {
                        result = handleWideIndexUpdateBySlaves(datas, pipeline, tableIdWideList, OperateType.UPDATE);
                    }
                    break;
                default:
                    log(WARN, logger, () -> "=loadWideIndex=>operateType:%s", eventType.getName());
                    result = datas.stream().mapToInt(value -> 1).toArray();
                    break;
            }
            return result;
        } catch (QueryPhaseExecutionException e) {
            e.printStackTrace();
            log(ERROR, logger, () -> "=loadWideIndex=>QueryPhaseExecutionException, operateType:%s , errors:%s", eventType.getName(), e);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            log(ERROR, logger, () -> "=loadWideIndex=>operateType:%s , errors:%s", eventType.getName(), e);
            throw new RuntimeException("=loadWideIndex=>error: ", e);
        }

    }

    private int[] handleWideIndexUpdateBySlaves(List<EventData> datas, Pipeline pipeline, List<WideTable> slaveWidetables, OperateType type) {
        List<CompletableFuture<int[]>> futures = Lists.newArrayList();
        slaveWidetables.forEach(slaveTable -> {
            CompletableFuture<int[]> future = CompletableFuture.supplyAsync(() -> {
                //FIXME datas is threadsafe ?clone it ok
                List<WideTable> wideTables = configClientService.findWideTable(slaveTable.getTarget().getId(), slaveTable.getMainTable().getId());
                return eventDataIndexService.batchWideUpdateBySlave(slaveTable.getTarget().getNamespace(), slaveTable.getTarget().getName(), datas, wideTables, slaveTable, pipeline, type);
            }, TheadPoolUtils.getInstance().executors);
            futures.add(future);
        });
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        List<Throwable> exceptions = new ArrayList(1);
        if (allOf.isCompletedExceptionally()) {
            allOf.exceptionally(e -> {
                exceptions.add(e);
                return null;
            });
            log(ERROR, logger, () -> "=handleWideIndexUpdateBySlaves=>error:%s ,type:%s , slave table name :%s , pipeline name:%s", () -> {
                return new Object[]{exceptions.get(0), type, slaveWidetables.get(0).getSlaveTable().getName(), pipeline.getName()};
            });
            if (!exceptions.isEmpty()) {
                throw new IndexLoadException(LogUtils.format("=handleWideIndexUpdateBySlaves=>error:%s ,type:%s , slave table name :%s , pipeline name:%s", exceptions.get(0), type, slaveWidetables.get(0).getSlaveTable().getName(), pipeline.getName()));
            }
        }
        return datas.stream().mapToInt(value -> 1).toArray();
    }

    private boolean isMasterTableId(Long tableId, List<WideTable> tableIdWideList) {
        List<WideTable> masterTables = tableIdWideList.stream().filter(wideTable -> wideTable.getMainTable().getId().equals(tableId)).collect(Collectors.toList());
        return !CollectionUtils.isEmpty(masterTables);
    }

    public void publish(IndexEvent event) {
        //FIXME todo
    }

    @Override
    public void destroy() {

    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setEventDataIndexService(IEventDataIndexService eventDataIndexService) {
        this.eventDataIndexService = eventDataIndexService;
    }

    public void setIndexConfigServiceFactory(IndexConfigServiceFactory indexConfigServiceFactory) {
        this.indexConfigServiceFactory = indexConfigServiceFactory;
    }
}
