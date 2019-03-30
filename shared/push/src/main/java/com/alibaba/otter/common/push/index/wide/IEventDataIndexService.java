package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.etl.model.EventData;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;

public interface IEventDataIndexService extends DisposableBean {

    int[] batchWideSaveByIds(String index, String type, List<EventData> list, List<WideTable> allWideTables, Pipeline pipeline);

    int[] batchWideUpdateByIds(String index, String type, List<EventData> list, List<WideTable> allWideTables, Pipeline pipeline);

    int[] batchWideUpdateBySlave(String wideIndex, String type, List<EventData> slaveList, List<WideTable> allWideTables, WideTable slaveWideTable, Pipeline pipeline, OperateType operateType);

    int[] batchSingleSaveByIds(String index, String type, List<EventData> list);

    int[] batchSingleUpdateByIds(String index, String type, List<EventData> list);

    int[] batchDeleteByIds(String index, String type, List<EventData> list);

    void deleteById(String index, String type, EventData eventData, String pkidName);

    void updateById(String index, String type, EventData eventData, String pkidName);

    void saveById(String index, String type, EventData eventData, String pkidName);

}
