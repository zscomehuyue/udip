package com.alibaba.otter.node.etl.common.alarm.state;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class StateService implements ApplicationListener {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        //state all alarm msg;
    }
}
