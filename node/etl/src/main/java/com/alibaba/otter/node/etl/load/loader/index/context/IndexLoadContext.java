package com.alibaba.otter.node.etl.load.loader.index.context;

import com.alibaba.otter.common.push.index.wide.ILoadIndexService;
import com.alibaba.otter.node.etl.load.loader.AbstractLoadContext;
import com.alibaba.otter.shared.etl.model.EventData;
import lombok.Data;

@Data
public class IndexLoadContext extends AbstractLoadContext<EventData> {
    private ILoadIndexService defaultLoadIndexService;

}
