package com.alibaba.otter.shared.arbitrate.impl.setl.memory;

import com.alibaba.otter.shared.arbitrate.impl.config.ArbitrateConfigUtils;
import com.alibaba.otter.shared.arbitrate.impl.setl.ArbitrateFactory;
import com.alibaba.otter.shared.arbitrate.impl.setl.IndexLoadArbitrateEvent;
import com.alibaba.otter.shared.arbitrate.impl.setl.monitor.PermitMonitor;
import com.alibaba.otter.shared.arbitrate.model.EtlEventData;
import com.alibaba.otter.shared.arbitrate.model.TerminEventData;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.enums.StageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Date;

public class IndexLoadMemoryArbitrateEvent implements IndexLoadArbitrateEvent {
    private static final Logger logger = LoggerFactory.getLogger(IndexLoadMemoryArbitrateEvent.class);
    private TerminMemoryArbitrateEvent terminEvent;
    private boolean endStageType = true;

    public EtlEventData await(Long pipelineId) throws InterruptedException {
        Assert.notNull(pipelineId);
        PermitMonitor permitMonitor = ArbitrateFactory.getInstance(pipelineId, PermitMonitor.class);
        permitMonitor.waitForPermit();// 阻塞等待授权
        MemoryStageController stageController = ArbitrateFactory.getInstance(pipelineId, MemoryStageController.class);
        Long processId = stageController.waitForProcess(StageType.INDEXLOAD); // 符合条件的processId
        ChannelStatus status = permitMonitor.getChannelPermit();
        if (status.isStart()) {// 即时查询一下当前的状态，状态随时可能会变
            return stageController.getLastData(processId);
        } else {
            logger.warn("pipelineId[{}] service load ignore processId[{}] by status[{}]", new Object[]{pipelineId, processId, status});
            return await(pipelineId);
        }
    }

    public void single(EtlEventData data) {
        Assert.notNull(data);
        data.setEndTime(new Date().getTime());// 返回当前时间
        MemoryStageController stageController = ArbitrateFactory.getInstance(data.getPipelineId(), MemoryStageController.class);
        boolean result = stageController.single(StageType.INDEXLOAD, data);// 通知下一个节点
        if (result && endStageType) {// 可能已经被rollback了，需要直接忽略
            // 调用Termin信号
            TerminEventData termin = new TerminEventData();
            termin.setPipelineId(data.getPipelineId());
            termin.setProcessId(data.getProcessId());
            termin.setStartTime(data.getStartTime());
            termin.setEndTime(data.getEndTime());
            termin.setFirstTime(data.getFirstTime());
            termin.setNumber(data.getNumber());
            termin.setBatchId(data.getBatchId());
            termin.setSize(data.getSize());
            termin.setExts(data.getExts());
            termin.setType(TerminEventData.TerminType.NORMAL);
            termin.setCode("setl");
            termin.setDesc("");
            termin.setCurrNid(ArbitrateConfigUtils.getCurrentNid());
            terminEvent.single(termin);

        }
    }

    public void setTerminEvent(TerminMemoryArbitrateEvent terminEvent) {
        this.terminEvent = terminEvent;
    }
}
