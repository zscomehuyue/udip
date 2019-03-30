package com.alibaba.otter.shared.arbitrate.impl.setl.delegate;

import com.alibaba.otter.shared.arbitrate.impl.setl.MqLoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;

import java.util.Map;

public class MqLoadDelegateArbitrateEvent  extends AbstractDelegateArbitrateEvent implements MqLoadArbitrateEvent{
    private Map<PipelineParameter.ArbitrateMode, MqLoadArbitrateEvent> delegate;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        return delegate.get(chooseMode(pipelineId)).await(pipelineId);
    }

    public void single(EtlEventData data) {
        delegate.get(chooseMode(data.getPipelineId())).single(data);
    }

    public void setDelegate(Map<PipelineParameter.ArbitrateMode, MqLoadArbitrateEvent> delegate) {
        this.delegate = delegate;
    }
}
