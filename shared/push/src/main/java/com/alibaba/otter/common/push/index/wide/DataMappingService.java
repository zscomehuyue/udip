package com.alibaba.otter.common.push.index.wide;

import org.elasticsearch.client.transport.TransportClient;

@FunctionalInterface
public interface DataMappingService {

    String createIndexWithMapping(String index, String dynamicIndex, TransportClient client);
}
