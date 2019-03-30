package com.alibaba.otter.node.etl.common.mq;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MqMessage implements Serializable {
    private static final long serialVersionUID = -6015993705757915935L;
    private String appId = "udip";
    private long msgId;
    private Map<String, String> headExt;
    private MessageHead msgHead = new MessageHead();
    private MsgBody msgBody = new MsgBody();

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public Map<String, String> getHeadExt() {
        return headExt;
    }

    public void setHeadExt(Map<String, String> headExt) {
        this.headExt = headExt;
    }

    public MessageHead getMsgHead() {
        return msgHead;
    }

    public MsgBody getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(MsgBody msgBody) {
        this.msgBody = msgBody;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this/*, OtterToStringStyle.DEFAULT_STYLE*/);
    }

    public static class MessageHead implements Serializable {
        private static final long serialVersionUID = -2873527170017506451L;
        private String tableName;
        private String schemaName;
        private long tableId;
        private long executeTime;
        private OperateType operateType;
        private SyncModel syncModel;
        private String sql;


        public String getTableName() {
            return tableName;
        }

        public MessageHead setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public MessageHead setSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return this;
        }

        public long getTableId() {
            return tableId;
        }

        public MessageHead setTableId(long tableId) {
            this.tableId = tableId;
            return this;
        }

        public OperateType getOperateType() {
            return operateType;
        }

        public MessageHead setOperateType(OperateType operateType) {
            this.operateType = operateType;
            return this;
        }

        public SyncModel getSyncModel() {
            return syncModel;
        }

        public MessageHead setSyncModel(SyncModel syncModel) {
            this.syncModel = syncModel;
            return this;
        }

        public String getSql() {
            return sql;
        }

        public MessageHead setSql(String sql) {
            this.sql = sql;
            return this;
        }

        public long getExecuteTime() {
            return executeTime;
        }

        public MessageHead setExecuteTime(long executeTime) {
            this.executeTime = executeTime;
            return this;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this/*, OtterToStringStyle.DEFAULT_STYLE*/);
        }


    }

    public enum SyncModel {
        /**
         * 行记录
         */
        ROW("R"),
        /**
         * 字段记录
         */
        FIELD("F");

        private String value;

        SyncModel(String value) {
            this.value = value;
        }

        public static SyncModel valueOfType(String name) {
            for (SyncModel type : values()) {
                if (type.getValue().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isRow() {
            return this.equals(ROW);
        }

        public boolean isField() {
            return this.equals(FIELD);
        }
    }

    public enum OperateType {

        /**
         * Insert row.
         */
        INSERT("I"),

        /**
         * Update row.
         */
        UPDATE("U"),

        /**
         * Delete row.
         */
        DELETE("D"),

        /**
         * Create table.
         */
        CREATE("C"),

        /**
         * Alter table.
         */
        ALTER("A"),

        /**
         * Erase table.
         */
        ERASE("E"),

        /**
         * Query.
         */
        QUERY("Q"),

        /**
         * Truncate.
         */
        TRUNCATE("T"),

        /**
         * rename.
         */
        RENAME("R"),

        /**
         * create index.
         */
        CINDEX("CI"),

        /**
         * drop index.
         */
        DINDEX("DI");

        private String value;

        OperateType(String value) {
            this.value = value;
        }

        public boolean isInsert() {
            return this.equals(INSERT);
        }

        public boolean isUpdate() {
            return this.equals(UPDATE);
        }

        public boolean isDelete() {
            return this.equals(DELETE);
        }

        public boolean isCreate() {
            return this.equals(CREATE);
        }

        public boolean isAlter() {
            return this.equals(ALTER);
        }

        public boolean isErase() {
            return this.equals(ERASE);
        }

        public boolean isQuery() {
            return this.equals(QUERY);
        }

        public boolean isTruncate() {
            return this.equals(TRUNCATE);
        }

        public boolean isRename() {
            return this.equals(RENAME);
        }

        public boolean isCindex() {
            return this.equals(CINDEX);
        }

        public boolean isDindex() {
            return this.equals(DINDEX);
        }

        public boolean isDdl() {
            return isCreate() || isAlter() || isErase() || isTruncate() || isRename() || isCindex() || isDindex();
        }

        public boolean isDml() {
            return isInsert() || isUpdate() || isDelete();
        }

        public static OperateType valueOfType(String name) {
            for (OperateType type : values()) {
                if (type.getValue().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    public class MsgBody implements Serializable {
        private static final long serialVersionUID = 774828183584562305L;

        private List<Column> oldKeys = new ArrayList<Column>();

        /**
         * 变更后的主键值,如果是insert/delete变更前和变更后的主键值是一样的.
         */
        private List<Column> keys = new ArrayList<Column>();

        /**
         * 非主键的其他字段
         */
        private List<Column> columns = new ArrayList<Column>();

        public List<Column> getOldKeys() {
            return oldKeys;
        }

        public MsgBody setOldKeys(List<Column> oldKeys) {
            this.oldKeys = oldKeys;
            return this;
        }

        public List<Column> getKeys() {
            return keys;
        }

        public MsgBody setKeys(List<Column> keys) {
            this.keys = keys;
            return this;
        }

        public List<Column> getColumns() {
            return columns;

        }

        public MsgBody setColumns(List<Column> columns) {
            this.columns = columns;
            return this;
        }


    }

    public static class Column implements Serializable {
        private static final long serialVersionUID = -1914960339611288301L;
        private int index;
        private int columnType;
        private String columnName;
        /**
         * timestamp,Datetime是一个long型的数字.
         */
        private String columnValue;
        private boolean isNull;
        private boolean isKey;

        /**
         * 2012.08.09 add by ljh , 新加字段，用于表明是否为真实变更字段，只针对非主键字段有效<br>
         * 因为FileResolver/EventProcessor会需要所有字段数据做分析，但又想保留按需字段同步模式
         * <p>
         * <pre>
         * 可以简单理解isUpdate代表是否需要在目标库执行数据变更，针对update有效，默认insert/delete为true
         * 1. row模式，所有字段均为updated
         * 2. field模式，通过db反查得到的结果，均为updated
         * 3. 其余场景，根据判断是否变更过，设置updated数据
         * </pre>
         */
        private boolean isUpdate = true;


        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getColumnType() {
            return columnType;
        }

        public void setColumnType(int columnType) {
            this.columnType = columnType;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnValue() {
            return columnValue;
        }

        public void setColumnValue(String columnValue) {
            this.columnValue = columnValue;
        }

        public boolean isNull() {
            return isNull;
        }

        public void setNull(boolean aNull) {
            isNull = aNull;
        }

        public boolean isKey() {
            return isKey;
        }

        public void setKey(boolean key) {
            isKey = key;
        }

        public boolean isUpdate() {
            return isUpdate;
        }

        public void setUpdate(boolean update) {
            isUpdate = update;
        }
    }

}
