package com.alibaba.otter.shared.arbitrate.impl.setl;

import com.alibaba.otter.shared.arbitrate.impl.ArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;

public interface MqLoadArbitrateEvent extends ArbitrateEvent {

    EtlEventData await(Long pipelineId) throws InterruptedException;

    void single(EtlEventData data);

}
