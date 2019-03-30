package com.alibaba.otter.node.etl.common.event.service;

import com.alibaba.otter.common.push.index.wide.event.IndexEventHandle;
import com.alibaba.otter.common.push.index.wide.event.IndexEvent;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.common.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.*;
import org.springframework.util.CollectionUtils;

import java.util.Map;

import static com.alibaba.otter.shared.common.utils.LogUtils.ERROR;
import static com.alibaba.otter.shared.common.utils.LogUtils.INFO;

public class IndexEventBusService implements ApplicationListener<IndexEvent>, ApplicationEventPublisherAware, ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(IndexEvent event) {
        LogUtils.log(INFO, logger, () -> "=onApplicationEvent=>indexEvent=%s", event.getType().toString());
        Map<String, IndexEventHandle> beans = applicationContext.getBeansOfType(event.getHandleClass());
        if (!CollectionUtils.isEmpty(beans)) {
            beans.forEach((s, indexEventHandle) -> {
                try {
                    LogUtils.log(INFO, logger, () -> "=onApplicationEvent=>handleEvent,indexEvent=%s", event.getType().toString());
                    indexEventHandle.handleEvent(event);
                } catch (Exception e) {
                    LogUtils.log(ERROR, logger, () -> "=onApplicationEvent=>indexEvent=%s , error:%s", event, e);
                }
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Assert.assertNotNull(applicationContext, "applicationContext is null.");
        this.applicationContext = applicationContext;
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    }
}
