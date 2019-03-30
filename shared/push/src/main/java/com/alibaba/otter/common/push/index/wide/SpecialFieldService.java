package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.common.push.index.type.OperateType;

import java.util.Map;

public interface SpecialFieldService {

    Map<String, Object> handleFields(Map<String, Object> dataMap,OperateType type);
}
