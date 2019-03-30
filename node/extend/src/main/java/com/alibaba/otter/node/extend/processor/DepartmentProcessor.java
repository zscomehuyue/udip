package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.common.push.index.wide.config.FieldMapping;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.etl.extend.processor.EventProcessor;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

import java.util.ArrayList;
import java.util.List;

public class DepartmentProcessor implements EventProcessor {

    @Override
    public boolean process(EventData eventData, DataMediaPair dataMediaPair) {
        List<EventColumn> list = new ArrayList(FieldMapping.DEPARTMENT_INCLUDE_FIELD.size());
        eventData.getColumns().forEach(eventColumn -> {
            if (null != FieldMapping.DEPARTMENT_INCLUDE_FIELD.get(eventColumn.getColumnName())) {
                eventColumn.setColumnName(FieldMapping.DEPARTMENT_INCLUDE_FIELD.get(eventColumn.getColumnName()));
                list.add(eventColumn);
            }
        });
        list.add(FieldHelper.createCityColumn(dataMediaPair));
        eventData.getColumns().clear();
        eventData.setColumns(list);

        List<EventColumn> keys = new ArrayList(eventData.getKeys().size());
        eventData.getKeys().forEach(eventColumn -> {
            if (null != FieldMapping.DEPARTMENT_INCLUDE_FIELD.get(eventColumn.getColumnName())) {
                eventColumn.setColumnName(FieldMapping.DEPARTMENT_INCLUDE_FIELD.get(eventColumn.getColumnName()));
                keys.add(eventColumn);
            }
        });
        eventData.getKeys().clear();
        eventData.setKeys(keys);
        return true;
    }
}
