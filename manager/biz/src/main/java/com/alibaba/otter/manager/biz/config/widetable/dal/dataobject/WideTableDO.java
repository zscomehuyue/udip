package com.alibaba.otter.manager.biz.config.widetable.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

public class WideTableDO implements Serializable {
    private static final long serialVersionUID = 3105158044381427671L;
    private Long id;
    private Long targetId;
    private String wideTableName;
    private Long mainTableId;
    private Long slaveTableId;
    private String mainTablePkIdName;
    private String slaveTablePkIdName;
    private String mainTableFkIdName;
    private String slaveTableFkIdName;

    //add
    private String slaveMainTablePkIdName;
    @Deprecated
    private Long realFkIdTableId;
    private String description;
    private Date created;
    private Date modified;

    public String getSlaveMainTablePkIdName() {
        return slaveMainTablePkIdName;
    }

    public void setSlaveMainTablePkIdName(String slaveMainTablePkIdName) {
        this.slaveMainTablePkIdName = slaveMainTablePkIdName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWideTableName() {
        return wideTableName;
    }

    public void setWideTableName(String wideTableName) {
        this.wideTableName = wideTableName;
    }

    public Long getMainTableId() {
        return mainTableId;
    }

    public void setMainTableId(Long mainTableId) {
        this.mainTableId = mainTableId;
    }

    public String getMainTablePkIdName() {
        return mainTablePkIdName;
    }

    public void setMainTablePkIdName(String mainTablePkIdName) {
        this.mainTablePkIdName = mainTablePkIdName;
    }

    public Long getSlaveTableId() {
        return slaveTableId;
    }

    public void setSlaveTableId(Long slaveTableId) {
        this.slaveTableId = slaveTableId;
    }

    public String getMainTableFkIdName() {
        return mainTableFkIdName;
    }

    public void setMainTableFkIdName(String mainTableFkIdName) {
        this.mainTableFkIdName = mainTableFkIdName;
    }

    public Long getRealFkIdTableId() {
        return realFkIdTableId;
    }

    public void setRealFkIdTableId(Long realFkIdTableId) {
        this.realFkIdTableId = realFkIdTableId;
    }

    public String getSlaveTableFkIdName() {
        return slaveTableFkIdName;
    }

    public void setSlaveTableFkIdName(String slaveTableFkIdName) {
        this.slaveTableFkIdName = slaveTableFkIdName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getSlaveTablePkIdName() {
        return slaveTablePkIdName;
    }

    public void setSlaveTablePkIdName(String slaveTablePkIdName) {
        this.slaveTablePkIdName = slaveTablePkIdName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WideTableDO that = (WideTableDO) o;

        if (!targetId.equals(that.targetId)) return false;
        if (!mainTableId.equals(that.mainTableId)) return false;
        return slaveTableId.equals(that.slaveTableId);
    }

    @Override
    public int hashCode() {
        int result = targetId.hashCode();
        result = 31 * result + mainTableId.hashCode();
        result = 31 * result + slaveTableId.hashCode();
        return result;
    }
}
