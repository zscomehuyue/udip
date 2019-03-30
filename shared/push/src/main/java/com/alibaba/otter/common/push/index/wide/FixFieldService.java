package com.alibaba.otter.common.push.index.wide;


import com.alibaba.otter.common.push.index.type.OperateType;

import java.util.Map;

public interface FixFieldService {

    Map<String, Object> getFixFields(OperateType type);
}
