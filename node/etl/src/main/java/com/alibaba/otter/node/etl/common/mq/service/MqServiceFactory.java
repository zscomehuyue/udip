package com.alibaba.otter.node.etl.common.mq.service;

import com.alibaba.otter.node.etl.common.mq.IEventDataMqService;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.google.common.collect.OtterMigrateMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;
import java.util.Optional;

public class MqServiceFactory implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(MqServiceFactory.class);

    private IEventDataMqService defaultRabbitService;
    private Map<Long, Map<MqMediaSource, IEventDataMqService>> services;

    public MqServiceFactory() {
        services = OtterMigrateMap.makeSoftValueComputingMap(
                new Function<Long, Map<MqMediaSource, IEventDataMqService>>() {

                    public Map<MqMediaSource, IEventDataMqService> apply(final Long pipelineId) {
                        // 构建第二层map
                        return MigrateMap.makeComputingMap(new Function<MqMediaSource, IEventDataMqService>() {

                            public IEventDataMqService apply(final MqMediaSource source) {
                                if (source.getType().isRabbit()) {
                                    //FIXME 动态创建客户端
                                } else if (source.getType().isKafka()) {

                                }

                                return null;
                            }
                        });
                    }
                });

    }

    public IEventDataMqService getMqService(Long pipelineId, MqMediaSource source) {
        return services.get(pipelineId).get(source);
    }

    public IEventDataMqService getDefaultRabbitService() {
        return defaultRabbitService;
    }

    public void setDefaultRabbitService(IEventDataMqService defaultRabbitService) {
        this.defaultRabbitService = defaultRabbitService;
    }

    @Override
    public void destroy() throws Exception {
        Optional.ofNullable(services).ifPresent((maps) -> {
            maps.forEach((pipleId, mapClient) -> {
                mapClient.forEach((source, client) -> {
                    try {
                        client.destroy();
                        log.warn("destroy client: key=" + source.getUrl());
                    } catch (Exception e) {
                    }
                });
            });
        });
    }
}