package com.alibaba.otter.common.push.index.es;

public interface IMappingService {
    Object getEsObject(int mysqlType, String data);
}
