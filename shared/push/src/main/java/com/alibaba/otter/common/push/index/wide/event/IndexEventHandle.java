package com.alibaba.otter.common.push.index.wide.event;

public interface IndexEventHandle {
    void handleEvent(IndexEvent event);

    default int order() {
        return -1;
    }

    default boolean threadSafe() {
        return false;
    }
}
