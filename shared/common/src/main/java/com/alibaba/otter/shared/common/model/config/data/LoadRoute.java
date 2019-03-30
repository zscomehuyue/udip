package com.alibaba.otter.shared.common.model.config.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoadRoute implements Serializable {
    private static final long serialVersionUID = 4743935178937656300L;
    private Long id;
    private Long pipelineId;
    private DataMedia table; // targetId
    private DataMedia loadDataMedia;
    private LoadType type;
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

    public DataMedia getTable() {
        return table;
    }

    public void setTable(DataMedia table) {
        this.table = table;
    }

    public DataMedia getLoadDataMedia() {
        return loadDataMedia;
    }

    public void setLoadDataMedia(DataMedia loadDataMedia) {
        this.loadDataMedia = loadDataMedia;
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

    public LoadType getType() {
        return type;
    }

    public void setType(LoadType type) {
        this.type = type;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}
