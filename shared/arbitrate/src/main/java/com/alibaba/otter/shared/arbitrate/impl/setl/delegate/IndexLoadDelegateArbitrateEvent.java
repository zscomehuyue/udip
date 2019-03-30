package com.alibaba.otter.shared.arbitrate.impl.setl.delegate;

import com.alibaba.otter.shared.arbitrate.impl.setl.IndexLoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.PipelineParameter;

import java.util.Map;

public class IndexLoadDelegateArbitrateEvent extends AbstractDelegateArbitrateEvent implements IndexLoadArbitrateEvent {

    private Map<PipelineParameter.ArbitrateMode, IndexLoadArbitrateEvent> delegate;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        return delegate.get(chooseMode(pipelineId)).await(pipelineId);
    }

    public void single(EtlEventData data) {
        delegate.get(chooseMode(data.getPipelineId())).single(data);
    }

    public void setDelegate(Map<PipelineParameter.ArbitrateMode, IndexLoadArbitrateEvent> delegate) {
        this.delegate = delegate;
    }

}
