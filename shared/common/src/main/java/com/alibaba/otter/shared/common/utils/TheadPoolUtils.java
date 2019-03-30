package com.alibaba.otter.shared.common.utils;

import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TheadPoolUtils {

    public ExecutorService executors;

    private TheadPoolUtils() {
        executors = new ThreadPoolExecutor(100,
                100,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(100 * 4),
                new NamedThreadFactory("-supplyAsync-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static class PoolHolder {
        private static TheadPoolUtils theadPoolUtils = new TheadPoolUtils();
    }

    public static TheadPoolUtils getInstance() {
        return PoolHolder.theadPoolUtils;
    }

    public static void main(String[] args) {
        ExecutorService executors = TheadPoolUtils.getInstance().executors;
        System.out.println(executors);
    }
}
