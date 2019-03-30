package com.alibaba.otter.node.etl.common.mq;

import com.alibaba.otter.shared.etl.model.EventData;
import org.springframework.beans.factory.DisposableBean;

public interface IEventDataMqService extends DisposableBean {
    void sendMessage(String destination, EventData eventData);
}
