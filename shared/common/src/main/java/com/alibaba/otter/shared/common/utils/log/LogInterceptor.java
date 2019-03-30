package com.alibaba.otter.shared.common.utils.log;

import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;

import static com.alibaba.otter.shared.common.utils.LogUtils.*;

public class LogInterceptor implements MethodInterceptor, ApplicationEventPublisherAware, BeanFactoryAware {
    private final Logger logger = LoggerFactory.getLogger(LogInterceptor.class);
    private ApplicationEventPublisher publisher;
    private BeanFactory beanFactory;
    private boolean publish = false;
    private long error = 200;
    private long warn = 100;
    private long info = 50;


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //FIXME value set null but have value ;
        LogMsg log = new LogMsg();
        log.setClassName(invocation.getThis().getClass().getName());
        log.setMethodName(invocation.getMethod().getName());
        log.setParams(invocation.getArguments());
        log.setStart(System.currentTimeMillis());

        if (invocation.getThis().getClass().getSimpleName().equals("MemoryEventStoreWithBuffer") &&
                log.getMethodName().equals("tryPut")
                && log.getParams().length == 1
                && log.getParams()[0].getClass().getSimpleName().equals("List")
                ) {

        }
        try {
            return invocation.proceed();
        } catch (Exception e) {
            LogUtils.log(ERROR, logger, () -> "=LogInterceptor=>invoke", e);
            log.setStack(ExceptionUtils.getMessage(e));
            throw e;
        } finally {
            log.setEnd(System.currentTimeMillis());
            log(log.getEnd() - log.getStart(), log.toMsg());
            //publishEvent(log);
        }
    }


    @Async
    public void publishEvent(LogMsg logMsg) {
        if (isPublish()) {
            publisher.publishEvent(new LogEvent(JsonUtils.marshalToByte(logMsg)));
        }
    }

    protected void log(long spend, String msg) {
        if (spend > this.error) {
            LogUtils.log(ERROR, logger, () -> msg);
        } else if (spend > this.warn) {
            LogUtils.log(WARN, logger, () -> msg);
        } else if (spend > this.info) {
            LogUtils.log(INFO, logger, () -> msg);
        } else {
            LogUtils.log(DEBUG, logger, () -> msg);
        }
    }

    public long getInfo() {
        return info;
    }

    public void setInfo(long info) {
        this.info = info;
    }

    public long getWarn() {
        return warn;
    }

    public void setWarn(long warn) {
        this.warn = warn;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublish() {
        return publish;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    @FunctionalInterface
    interface LogAround {
        Object around(MethodInvocation invocation) throws Throwable;
    }
}

