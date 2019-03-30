package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.shared.common.model.config.data.WideTable;

import java.util.List;
import java.util.Map;

public interface ValidFieldService {
    boolean valid(Map<String, Object> dataMap, List<WideTable> wideTables);
}
