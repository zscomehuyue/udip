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

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.mysql.MysqlDialect;
import com.alibaba.otter.node.etl.common.db.utils.SqlUtils;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadAction;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.node.etl.load.loader.db.context.DbLoadContext;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 数据库load的执行入口
 *
 * @author jianghang 2011-10-31 下午03:17:43
 * @version 4.0.0
 */
public class DbLoadAction extends AbstractLoadAction {

    private DbDialectFactory dbDialectFactory;

    public DbLoadAction() {
        super(DataMediaType.MYSQL,"DbLoadAction");
    }

    protected AbstractLoadContext buildContext(Identity identity) {
        DbLoadContext context = new DbLoadContext();
        context.setIdentity(identity);
        Channel channel = configClientService.findChannel(identity.getChannelId());
        Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
        context.setChannel(channel);
        context.setPipeline(pipeline);
        return context;
    }


    protected void setContext(AbstractLoadContext context, List<EventData> datas) {
        ((DbLoadContext) context).setDataMediaSource(ConfigHelper.findDataMedia(context.getPipeline(), datas.get(0).getTableId())
                .getSource());
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
     * 执行ddl的调用，处理逻辑比较简单: 串行调用
     *
     * @param context
     * @param eventDatas
     */
    protected void doDdl(AbstractLoadContext context, List<EventData> eventDatas) {
        for (final EventData data : eventDatas) {
            DataMedia dataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId());
            final DbDialect dbDialect = dbDialectFactory.getDbDialect(context.getIdentity().getPipelineId(),
                    (DbMediaSource) dataMedia.getSource());
            Boolean skipDdlException = context.getPipeline().getParameters().getSkipDdlException();
            try {
                Boolean result = dbDialect.getJdbcTemplate().execute(new StatementCallback<Boolean>() {

                    public Boolean doInStatement(Statement stmt) throws SQLException, DataAccessException {
                        Boolean result = false;
                        if (dbDialect instanceof MysqlDialect && StringUtils.isNotEmpty(data.getDdlSchemaName())) {
                            // 如果mysql，执行ddl时，切换到在源库执行的schema上
                            // result &= stmt.execute("use " +
                            // data.getDdlSchemaName());

                            // 解决当数据库名称为关键字如"Order"的时候,会报错,无法同步
                            result &= stmt.execute("use `" + data.getDdlSchemaName() + "`");
                        }
                        result &= stmt.execute(data.getSql());
                        return result;
                    }
                });
                if (result) {
                    context.getProcessedDatas().add(data); // 记录为成功处理的sql
                } else {
                    context.getFailedDatas().add(data);
                }

            } catch (Throwable e) {
                if (skipDdlException) {
                    // do skip
                    logger.warn("skip exception for ddl : {} , caused by {}", data, ExceptionUtils.getFullStackTrace(e));
                } else {
                    throw new LoadException(e);
                }
            }

        }
    }


    protected void doWorker(AbstractLoadContext context, boolean canBatch, List<Future<Exception>> results, List<EventData> rows,Long weight, OperateType type) {
        results.add(executor.submit(new DbLoadWorker(context, rows, canBatch)));
    }


    protected void repareWorker(AbstractLoadContext context, List<EventData> retryEventDatas, boolean canBath, Long weight, OperateType type) {
        DbLoadWorker worker = new DbLoadWorker(context, retryEventDatas, false);// 强制设置batch为false
        try {
            Exception ex = worker.call();
            if (ex != null) {
                throw ex; // 自己抛自己接
            }
        } catch (Exception ex) {
            logger.error("##load phase two failed!", ex);
            throw new LoadException(ex);
        }
    }

    protected void repareWorker(AbstractLoadContext context, EventData eventData, boolean canBath,Long weight, OperateType type){
        DbLoadWorker worker = new DbLoadWorker(context, Arrays.asList(eventData), false);// 强制设置batch为false
        try {
            Exception ex = worker.call();
            if (ex != null) {
                // do skip
                logger.warn("skip exception for data : {} , caused by {}",
                        eventData,
                        ExceptionUtils.getFullStackTrace(ex));
            }
        } catch (Exception ex) {
            // do skip
            logger.warn("skip exception for data : {} , caused by {}",
                    eventData,
                    ExceptionUtils.getFullStackTrace(ex));
        }
    }

    class DbLoadWorker implements Callable<Exception> {
        private AbstractLoadContext context;
        private DbDialect dbDialect;
        private List<EventData> datas;
        private boolean canBatch;
        private List<EventData> allFailedDatas = new ArrayList<EventData>();
        private List<EventData> allProcesedDatas = new ArrayList<EventData>();
        private List<EventData> processedDatas = new ArrayList<EventData>();
        private List<EventData> failedDatas = new ArrayList<EventData>();

        public DbLoadWorker(AbstractLoadContext context, List<EventData> datas, boolean canBatch) {
            this.context = context;
            this.datas = datas;
            this.canBatch = canBatch;

            EventData data = datas.get(0); // eventData为同一数据库的记录，只取第一条即可
            DataMedia dataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId());
            dbDialect = dbDialectFactory.getDbDialect(context.getIdentity().getPipelineId(),
                    (DbMediaSource) dataMedia.getSource());

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
            int index = 0;// 记录下处理成功的记录下标
            for (; index < datas.size(); ) {
                // 处理数据切分
                final List<EventData> splitDatas = new ArrayList<EventData>();
                if (useBatch && canBatch) {
                    int end = (index + batchSize > datas.size()) ? datas.size() : (index + batchSize);
                    splitDatas.addAll(datas.subList(index, end));
                    index = end;// 移动到下一批次
                } else {
                    splitDatas.add(datas.get(index));
                    index = index + 1;// 移动到下一条
                }

                int retryCount = 0;
                while (true) {
                    try {
                        if (CollectionUtils.isEmpty(failedDatas) == false) {
                            splitDatas.clear();
                            splitDatas.addAll(failedDatas); // 下次重试时，只处理错误的记录
                        } else {
                            failedDatas.addAll(splitDatas); // 先添加为出错记录，可能获取lob,datasource会出错
                        }

                        final LobCreator lobCreator = dbDialect.getLobHandler().getLobCreator();
                        if (useBatch && canBatch) {
                            // 处理batch
                            final String sql = splitDatas.get(0).getSql();
                            int[] affects = new int[splitDatas.size()];
                            affects = (int[]) dbDialect.getTransactionTemplate().execute(new TransactionCallback() {

                                public Object doInTransaction(TransactionStatus status) {
                                    // 初始化一下内容
                                    try {
                                        failedDatas.clear(); // 先清理
                                        processedDatas.clear();
                                        interceptor.transactionBegin(context, splitDatas, dbDialect);
                                        JdbcTemplate template = dbDialect.getJdbcTemplate();
                                        int[] affects = template.batchUpdate(sql, new BatchPreparedStatementSetter() {

                                            public void setValues(PreparedStatement ps, int idx) throws SQLException {
                                                doPreparedStatement(ps, dbDialect, lobCreator, splitDatas.get(idx));
                                            }

                                            public int getBatchSize() {
                                                return splitDatas.size();
                                            }
                                        });
                                        interceptor.transactionEnd(context, splitDatas, dbDialect);
                                        return affects;
                                    } finally {
                                        lobCreator.close();
                                    }
                                }

                            });

                            // 更新统计信息
                            for (int i = 0; i < splitDatas.size(); i++) {
                                processStat(splitDatas.get(i), affects[i], true);
                            }
                        } else {
                            final EventData data = splitDatas.get(0);// 直接取第一条
                            int affect = 0;
                            affect = (Integer) dbDialect.getTransactionTemplate().execute(new TransactionCallback() {

                                public Object doInTransaction(TransactionStatus status) {
                                    try {
                                        failedDatas.clear(); // 先清理
                                        processedDatas.clear();
                                        interceptor.transactionBegin(context, Arrays.asList(data), dbDialect);
                                        JdbcTemplate template = dbDialect.getJdbcTemplate();
                                        int affect = template.update(data.getSql(), new PreparedStatementSetter() {

                                            public void setValues(PreparedStatement ps) throws SQLException {
                                                doPreparedStatement(ps, dbDialect, lobCreator, data);
                                            }
                                        });
                                        interceptor.transactionEnd(context, Arrays.asList(data), dbDialect);
                                        return affect;
                                    } finally {
                                        lobCreator.close();
                                    }
                                }
                            });
                            // 更新统计信息
                            processStat(data, affect, false);
                        }

                        error = null;
                        exeResult = ExecuteResult.SUCCESS;
                    } catch (DeadlockLoserDataAccessException ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                                DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.RETRY;
                    } catch (DataIntegrityViolationException ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                                DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    } catch (RuntimeException ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                                DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    } catch (Throwable ex) {
                        error = new LoadException(ExceptionUtils.getFullStackTrace(ex),
                                DbLoadDumper.dumpEventDatas(splitDatas));
                        exeResult = ExecuteResult.ERROR;
                    }

                    if (ExecuteResult.SUCCESS == exeResult) {
                        allFailedDatas.addAll(failedDatas);// 记录一下异常到all记录中
                        allProcesedDatas.addAll(processedDatas);
                        failedDatas.clear();// 清空上一轮的处理
                        processedDatas.clear();
                        break; // do next eventData
                    } else if (ExecuteResult.RETRY == exeResult) {
                        retryCount = retryCount + 1;// 计数一次
                        // 出现异常，理论上当前的批次都会失败
                        processedDatas.clear();
                        failedDatas.clear();
                        failedDatas.addAll(splitDatas);
                        if (retryCount >= retry) {
                            processFailedDatas(index);// 重试已结束，添加出错记录并退出
                            throw new LoadException(String.format("execute [%s] retry %s times failed",
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
                                throw new LoadException(ex);
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

        private void doPreparedStatement(PreparedStatement ps, DbDialect dbDialect, LobCreator lobCreator,
                                         EventData data) throws SQLException {
            EventType type = data.getEventType();
            // 注意insert/update语句对应的字段数序都是将主键排在后面
            List<EventColumn> columns = new ArrayList<EventColumn>();
            if (type.isInsert()) {
                columns.addAll(data.getColumns()); // insert为所有字段
                columns.addAll(data.getKeys());
            } else if (type.isDelete()) {
                columns.addAll(data.getKeys());
            } else if (type.isUpdate()) {
                boolean existOldKeys = !CollectionUtils.isEmpty(data.getOldKeys());
                columns.addAll(data.getUpdatedColumns());// 只更新带有isUpdate=true的字段
                columns.addAll(data.getKeys());
                if (existOldKeys) {
                    columns.addAll(data.getOldKeys());
                }
            }

            // 获取一下当前字段名的数据是否必填
            Table table = dbDialect.findTable(data.getSchemaName(), data.getTableName());
            Map<String, Boolean> isRequiredMap = new HashMap<String, Boolean>();
            for (Column tableColumn : table.getColumns()) {
                isRequiredMap.put(StringUtils.lowerCase(tableColumn.getName()), tableColumn.isRequired());
            }

            for (int i = 0; i < columns.size(); i++) {
                int paramIndex = i + 1;
                EventColumn column = columns.get(i);
                int sqlType = column.getColumnType();

                Boolean isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));
                if (isRequired == null) {
                    // 清理一下目标库的表结构,二次检查一下
                    table = dbDialect.findTable(data.getSchemaName(), data.getTableName(), false);
                    isRequiredMap = new HashMap<String, Boolean>();
                    for (Column tableColumn : table.getColumns()) {
                        isRequiredMap.put(StringUtils.lowerCase(tableColumn.getName()), tableColumn.isRequired());
                    }

                    isRequired = isRequiredMap.get(StringUtils.lowerCase(column.getColumnName()));
                    if (isRequired == null) {
                        throw new LoadException(String.format("column name %s is not found in Table[%s]",
                                column.getColumnName(),
                                table.toString()));
                    }
                }

                Object param = null;
                if (dbDialect instanceof MysqlDialect
                        && (sqlType == Types.TIME || sqlType == Types.TIMESTAMP || sqlType == Types.DATE)) {
                    // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                    // driver进行处理，如果转化为Timestamp会出错
                    param = column.getColumnValue();
                } else {
                    param = SqlUtils.stringToSqlValue(column.getColumnValue(),
                            sqlType,
                            isRequired,
                            dbDialect.isEmptyStringNulled());
                }

                try {
                    switch (sqlType) {
                        case Types.CLOB:
                            lobCreator.setClobAsString(ps, paramIndex, (String) param);
                            break;
                        case Types.BLOB:
                            lobCreator.setBlobAsBytes(ps, paramIndex, (byte[]) param);
                            break;
                        case Types.TIME:
                        case Types.TIMESTAMP:
                        case Types.DATE:
                            // 只处理mysql的时间类型，oracle的进行转化处理
                            if (dbDialect instanceof MysqlDialect) {
                                // 解决mysql的0000-00-00 00:00:00问题，直接依赖mysql
                                // driver进行处理，如果转化为Timestamp会出错
                                ps.setObject(paramIndex, param);
                            } else {
                                StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                            }
                            break;
                        case Types.BIT:
                            // 只处理mysql的bit类型，bit最多存储64位，所以需要使用BigInteger进行处理才能不丢精度
                            // mysql driver将bit按照setInt进行处理，会导致数据越界
                            if (dbDialect instanceof MysqlDialect) {
                                StatementCreatorUtils.setParameterValue(ps, paramIndex, Types.DECIMAL, null, param);
                            } else {
                                StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                            }
                            break;
                        default:
                            StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, null, param);
                            break;
                    }
                } catch (SQLException ex) {
                    logger.error("## SetParam error , [pairId={}, sqltype={}, value={}]",
                            new Object[]{data.getPairId(), sqlType, param});
                    throw ex;
                }
            }
        }

        private void processStat(EventData data, int affect, boolean batch) {
            if (batch && (affect < 1 && affect != Statement.SUCCESS_NO_INFO)) {
                failedDatas.add(data); // 记录到错误的临时队列，进行重试处理
            } else if (!batch && affect < 1) {
                failedDatas.add(data);// 记录到错误的临时队列，进行重试处理
            } else {
                processedDatas.add(data); // 记录到成功的临时队列，commit也可能会失败。所以这记录也可能需要进行重试
                DbLoadAction.this.processStat(data, context);
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

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }


}
