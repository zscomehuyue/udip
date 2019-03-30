package com.alibaba.otter.common.push.index.wide;

@FunctionalInterface
public interface FieldFormatService {

    String getFormatField(String tableName, String fileName);
}
