package com.alibaba.otter.shared.arbitrate.impl.setl.zookeeper;

import com.alibaba.otter.shared.arbitrate.impl.setl.MqLoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;

public class MqLoadZooKeeperArbitrateEvent implements MqLoadArbitrateEvent {
    @Override
    public EtlEventData await(Long pipelineId) throws InterruptedException {
        if (true) {
            throw new RuntimeException("arbitrateEvent not support zk.");
        }
        return null;
    }

    @Override
    public void single(EtlEventData data) {
        if (true) {
            throw new RuntimeException("arbitrateEvent not support zk.");
        }
    }
}
