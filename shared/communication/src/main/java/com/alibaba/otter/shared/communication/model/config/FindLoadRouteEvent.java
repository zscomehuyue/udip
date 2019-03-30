package com.alibaba.otter.shared.communication.model.config;

import com.alibaba.otter.shared.communication.core.model.Event;

public class FindLoadRouteEvent extends Event {
    private static final long serialVersionUID = -3101147002492614497L;

    public FindLoadRouteEvent() {
        super(ConfigEventType.findLoadRoute);
    }

}
