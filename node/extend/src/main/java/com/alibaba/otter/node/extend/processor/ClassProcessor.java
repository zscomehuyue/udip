package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.common.push.index.wide.config.FieldMapping;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.etl.extend.processor.EventProcessor;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassProcessor implements EventProcessor {


    @Override
    public boolean process(EventData eventData, DataMediaPair dataMediaPair) {
        Map<EventColumn, String> map = new ConcurrentHashMap(FieldMapping.CLASS_INCLUDE_FIELD.size());
        eventData.getColumns().parallelStream().forEach(eventColumn -> {
            if (null != FieldMapping.CLASS_INCLUDE_FIELD.get(eventColumn.getColumnName())) {
                eventColumn.setColumnName(FieldMapping.CLASS_INCLUDE_FIELD.get(eventColumn.getColumnName()));
                map.put(eventColumn, FieldMapping.NULL_VALUE);
            }
        });
        map.put(FieldHelper.createCityColumn(dataMediaPair),FieldMapping.NULL_VALUE);
        eventData.getColumns().clear();
        eventData.setColumns(Arrays.asList(map.keySet().toArray(new EventColumn[FieldMapping.CLASS_INCLUDE_FIELD.size()])));
        List<EventColumn> keys = new ArrayList(eventData.getKeys().size());
        eventData.getKeys().forEach(eventColumn -> {
            if (null != FieldMapping.CLASS_INCLUDE_FIELD.get(eventColumn.getColumnName())) {
                eventColumn.setColumnName(FieldMapping.CLASS_INCLUDE_FIELD.get(eventColumn.getColumnName()));
                keys.add(eventColumn);
            }
        });
        eventData.getKeys().clear();
        eventData.setKeys(keys);
        return true;
    }

}
