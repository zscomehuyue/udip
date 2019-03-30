package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.common.push.index.type.OperateType;

import java.util.Map;

public interface StatusFiledService {
    Map<String, Object> getStatusFields(OperateType type);
}
