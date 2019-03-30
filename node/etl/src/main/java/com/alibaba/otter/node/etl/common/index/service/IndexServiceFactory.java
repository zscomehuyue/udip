package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.common.push.index.wide.ILoadIndexService;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.es.IndexMediaSource;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.google.common.collect.OtterMigrateMap;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.Optional;

@Data
public class IndexServiceFactory implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IndexServiceFactory.class);

    private ILoadIndexService loadIndexService;
    private Map<Long, Map<IndexMediaSource, ILoadIndexService>> serviceCache;

    public IndexServiceFactory() {
        serviceCache = OtterMigrateMap.makeSoftValueComputingMap(
                new Function<Long, Map<IndexMediaSource, ILoadIndexService>>() {

                    public Map<IndexMediaSource, ILoadIndexService> apply(final Long pipelineId) {
                        // 构建第二层map
                        return MigrateMap.makeComputingMap(new Function<IndexMediaSource, ILoadIndexService>() {

                            public ILoadIndexService apply(final IndexMediaSource source) {
                                if (source.getType().isSolr()) {
                                    //FIXME 动态创建客户端
                                } else if (source.getType().isEs()) {

                                }
                                return null;
                            }
                        });
                    }
                });

    }

    public ILoadIndexService getLoadIndexService(Long pipelineId, DataMedia indexDataMedia) {
        ILoadIndexService service = (null == serviceCache.get(pipelineId) || serviceCache.get(pipelineId).size() <= 0) ? null : serviceCache.get(pipelineId).get((IndexMediaSource) indexDataMedia.getSource());
        return null == service ? loadIndexService : service;
    }

    @Override
    public void destroy() throws Exception {
        Optional.ofNullable(serviceCache).ifPresent((maps) -> {
            maps.forEach((pipleId, mapClient) -> {
                mapClient.forEach((source, client) -> {
                    try {
                        client.destroy();
                        log.warn("=destroy=>destroy client: key=" + source.getUrl());
                    } catch (Exception e) {
                    }
                });
            });
        });
    }
}
