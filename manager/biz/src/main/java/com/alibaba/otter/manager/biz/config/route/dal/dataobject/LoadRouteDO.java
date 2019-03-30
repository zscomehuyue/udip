package com.alibaba.otter.manager.biz.config.route.dal.dataobject;

import java.io.Serializable;
import java.util.Date;

public class LoadRouteDO implements Serializable {
    private static final long serialVersionUID = -7305649911198653648L;
    private Long id;
    private Long pipelineId;
    private Long tableId;
    private Long loadDataMediaId;
    private Integer type;
    private String description;
    private Date created;
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Long getLoadDataMediaId() {
        return loadDataMediaId;
    }

    public void setLoadDataMediaId(Long loadDataMediaId) {
        this.loadDataMediaId = loadDataMediaId;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}
