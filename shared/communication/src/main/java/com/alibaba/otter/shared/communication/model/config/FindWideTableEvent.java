package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

public class FindWideTableEvent extends Event {
    private static final long serialVersionUID = -3101147002492614497L;
    private Long tableId;
    private Long targetId;

    public FindWideTableEvent() {
        super(ConfigEventType.findWideTable);
    }

    public Long getTableId() {
        return tableId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }
}
