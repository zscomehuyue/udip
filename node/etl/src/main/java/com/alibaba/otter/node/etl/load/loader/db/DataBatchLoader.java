/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.node.etl.load.loader.db;

import com.alibaba.otter.node.etl.OtterConstants;
import com.alibaba.otter.node.etl.load.loader.AbstractDataBatchLoader;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.node.etl.load.loader.LoadContext;
import com.alibaba.otter.node.etl.load.loader.db.context.FileLoadContext;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.etl.model.FileBatch;
import com.alibaba.otter.shared.etl.model.RowBatch;
import org.slf4j.MDC;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * 针对RowData的数据载入实现
 *
 * @author jianghang 2011-10-27 上午11:15:48
 * @version 4.0.0
 */
public class DataBatchLoader extends AbstractDataBatchLoader {

    protected void submitFileBatch(List<Future<LoadContext>> futures, ExecutorCompletionService completionService,
                                   final FileBatch fileBatch, final File rootDir, final WeightController controller) {
        futures.add(completionService.submit(new Callable<FileLoadContext>() {

            public FileLoadContext call() throws Exception {
                try {
                    MDC.put(OtterConstants.splitPipelineLogFileKey,
                            String.valueOf(fileBatch.getIdentity().getPipelineId()));

                    FileLoadAction fileLoadAction = (FileLoadAction) beanFactory.getBean("fileLoadAction",
                            FileLoadAction.class);
                    return fileLoadAction.load(fileBatch, rootDir, controller);
                } finally {
                    MDC.remove(OtterConstants.splitPipelineLogFileKey);
                }
            }
        }));
    }

    protected void submitRowBatch(List<Future<LoadContext>> futures, ExecutorCompletionService completionService,
                                  final List<RowBatch> rowBatchs, final WeightController controller) {
        for (final RowBatch rowBatch : rowBatchs) {
            // 提交多个并行加载通道
            futures.add(completionService.submit(new Callable<AbstractLoadContext>() {

                public AbstractLoadContext call() throws Exception {
                    try {
                        MDC.put(OtterConstants.splitPipelineLogFileKey, String.valueOf(rowBatch.getIdentity().getPipelineId()));
                        // dbLoadAction是一个pool池化对象
                        DbLoadAction dbLoadAction = beanFactory.getBean("dbLoadAction", DbLoadAction.class);
                        return dbLoadAction.load(rowBatch, controller);
                    } finally {
                        MDC.remove(OtterConstants.splitPipelineLogFileKey);
                    }
                }
            }));
        }
    }

}
