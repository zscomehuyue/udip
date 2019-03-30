package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.shared.etl.model.EventColumn;

import java.util.Map;

@FunctionalInterface
public interface FieldTypeService {

    Map<String, Object> handleFieldType(FieldFormatService service, String tableName, EventColumn column);
}
