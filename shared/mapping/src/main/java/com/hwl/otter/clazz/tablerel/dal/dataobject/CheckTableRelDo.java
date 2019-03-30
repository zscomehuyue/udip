package com.hwl.otter.clazz.tablerel.dal.dataobject;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @Description: 数据核对表映射信息
 * @Author: tangdelong
 * @Date: 2018/6/21 16:34
 */
public class CheckTableRelDo implements Serializable {

    private static final long serialVersionUID = -3943607298533205644L;

    private String id;

    private String tableName;

    private String timeFieldName;

    private String keyName;

    private String whereSql;

    /**
     * 是否使用，0:使用，1：不能使用
     */
    private int isUse;

    private Timestamp createDate;

    private Timestamp updateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTimeFieldName() {
        return timeFieldName;
    }

    public void setTimeFieldName(String timeFieldName) {
        this.timeFieldName = timeFieldName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getWhereSql() {
        return whereSql;
    }

    public void setWhereSql(String whereSql) {
        this.whereSql = whereSql;
    }

    public int getIsUse() {
        return isUse;
    }

    public void setIsUse(int isUse) {
        this.isUse = isUse;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }
}
