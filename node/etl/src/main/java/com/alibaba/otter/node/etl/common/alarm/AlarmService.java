package com.alibaba.otter.node.etl.common.alarm;

import com.alibaba.otter.common.push.index.wide.event.IndexEvent;
import com.alibaba.otter.shared.common.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import static com.alibaba.otter.shared.common.utils.LogUtils.INFO;

public class AlarmService implements ApplicationListener<IndexEvent>, ApplicationEventPublisherAware {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationEventPublisher publisher;

    @Override
    public void onApplicationEvent(IndexEvent event) {
        LogUtils.log(INFO, log, () -> "=AlarmService=>eventType=%s", event.getType().toString());
//        if (event.getType() == ExtEventType.ALARM) {
//            LogUtils.log(INFO, log, () -> "=AlarmService=>type=%s ,data=%s", event.getType().toString(), event.getSource());
////            Arrays.stream(WarnType.values()).parallel().forEach(warnType -> publisher.publishEvent());
//        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
