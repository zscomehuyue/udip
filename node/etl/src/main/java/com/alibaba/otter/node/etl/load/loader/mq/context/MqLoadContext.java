package com.alibaba.otter.node.etl.load.loader.mq.context;

import com.alibaba.otter.node.etl.common.mq.IEventDataMqService;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.shared.etl.model.EventData;

public class MqLoadContext  extends AbstractLoadContext<EventData> {
    private IEventDataMqService defaultMqService;

    public IEventDataMqService getDefaultMqService() {
        return defaultMqService;
    }

    public void setDefaultMqService(IEventDataMqService defaultMqService) {
        this.defaultMqService = defaultMqService;
    }
}
