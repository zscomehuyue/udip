package com.alibaba.otter.common.push.index.wide;

import com.alibaba.otter.common.push.index.type.OperateType;

import java.util.List;
import java.util.Map;

public interface DynmaicIndexService {
    /**
     * @param index
     * @param type
     * @param pkidMap
     * @return first is index name ,second is pkIdMap
     */
    Map<String, Map<String, Map<String, Object>>> getDynmaicDataMap(String index, String type, OperateType eventType, Map<String, Map<String, Object>> pkidMap);

    Map<String, List<String>> getDynmaicDataMap(String index, String type, List<String> pkIds);
}
