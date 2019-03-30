package com.alibaba.otter.node.etl.common.event;

import com.alibaba.otter.common.push.index.wide.event.ExtEventType;
import org.springframework.context.ApplicationEvent;

public class AlarmEvent extends ApplicationEvent {
    private ExtEventType type;

    public AlarmEvent(Object source) {
        super(source);
    }

    public AlarmEvent(Object source, ExtEventType type) {
        this(source);
        this.type = type;
    }

    public ExtEventType getType() {
        return type;
    }

    public void setType(ExtEventType type) {
        this.type = type;
    }
}
