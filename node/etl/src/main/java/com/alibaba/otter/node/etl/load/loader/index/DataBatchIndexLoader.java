package com.alibaba.otter.node.etl.load.loader.index;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.load.loader.AbstractDataBatchLoader;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.node.etl.load.loader.LoadContext;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.etl.model.RowBatch;
import org.slf4j.MDC;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class DataBatchIndexLoader extends AbstractDataBatchLoader {
    protected AtomicLong count = new AtomicLong(0);

    protected void submitRowBatch(List<Future<LoadContext>> futures, ExecutorCompletionService completionService,
                                  final List<RowBatch> rowBatchs, final WeightController controller) {
        for (final RowBatch rowBatch : rowBatchs) {
            // 提交多个并行加载通道
            futures.add(completionService.submit(new Callable<AbstractLoadContext>() {

                public AbstractLoadContext call() throws Exception {
                    try {
                        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(rowBatch.getIdentity().getPipelineId()));
                        IndexLoadAction loadAction = beanFactory.getBean("indexLoadAction", IndexLoadAction.class);
                        return loadAction.load(rowBatch, controller);
                    } finally {
                        MDC.remove(OtterConstants.splitPipelineLogFileKey);
                    }
                }
            }));
        }
    }


}
