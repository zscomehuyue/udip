package com.alibaba.otter.common.push.index.wide.event;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class IndexEvent extends ApplicationEvent {

    private Class<IndexEventHandle> handleClass;
    private OperateType type;
    private DataMedia sourceDataMedia;
    private DataMedia indexDataMedia;

    public IndexEvent(Object source) {
        super(source);
    }

    public IndexEvent(Object source, OperateType type, DataMedia sourceDataMedia, DataMedia indexDataMedia, Class<IndexEventHandle> handleClass) {
        this(source);
        this.type = type;
        this.handleClass = handleClass;
        this.sourceDataMedia = sourceDataMedia;
        this.indexDataMedia = indexDataMedia;
    }

}
