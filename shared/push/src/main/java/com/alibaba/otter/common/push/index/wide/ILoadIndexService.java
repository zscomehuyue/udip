package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.event.IndexEvent;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventData;

import java.util.List;

/**
 * Index load service ;
 */
public interface ILoadIndexService {

    int[] loadSingleIndex(DataMedia indexDataMedia, List<EventData> datas, OperateType eventType);

    int[] loadWideIndex(DataMedia indexDataMedia, List<EventData> datas, OperateType eventType, Long tableId, Pipeline pipeline);

    void publish(IndexEvent event);

    void destroy();
}
