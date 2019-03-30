package com.alibaba.otter.shared.arbitrate.impl.setl.rpc;

import com.alibaba.otter.shared.arbitrate.impl.setl.MqLoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;

public class MqLoadRpcArbitrateEvent implements MqLoadArbitrateEvent {
    @Override
    public EtlEventData await(Long pipelineId) throws InterruptedException {
        throw new RuntimeException("not exist ");
    }

    @Override
    public void single(EtlEventData data) {

    }

}
