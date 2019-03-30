package com.alibaba.otter.shared.common.utils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

public enum LogUtils {
    INFO, DEBUG, WARN, ERROR;

    protected final static Logger logger = LoggerFactory.getLogger(LogUtils.class);

    public static String format(String msg, Object... args) {
        try {
            if (null == args) {
                return msg;
            }
            return String.format(msg, Arrays.stream(args).map(o -> {
                if (null == o) {
                    return "null";
                }
                if (Throwable.class.isAssignableFrom(o.getClass())) {
                    return ExceptionUtils.getFullStackTrace(((Throwable) o));
                } else if (String.class.isAssignableFrom(o.getClass())) {
                    return (String) o;
                } else if (null != findMethod(o.getClass(), "toString")) {
                    return o.toString();
                } else {
                    return ToStringBuilder.reflectionToString(o);
                }
            }).toArray());
        } catch (Exception e) {
            log(ERROR, logger, () -> getFullStackTrace(e));
        }
        return "LogUtils Format Error : Please check your msg format!";
    }

    public static String getFullStackTrace(Throwable ex) {
        return ExceptionUtils.getFullStackTrace(ex);
    }

    public static void log(LogUtils level, Logger logger, Supplier<String> supplier) {
        switch (level) {
            case DEBUG:
                if (logger.isDebugEnabled()) {
                    logger.debug(supplier.get());
                }
                break;
            case INFO:
                if (logger.isInfoEnabled()) {
                    logger.info(supplier.get());
                }
                break;
            case WARN:
                if (logger.isWarnEnabled()) {
                    logger.warn(supplier.get());
                }
                break;
            case ERROR:
                if (logger.isErrorEnabled()) {
                    logger.error(supplier.get());
                }
                break;
        }
    }

    public static void log(LogUtils level, Logger logger, Supplier<String> supplier, Object... formatObj) {
        switch (level) {
            case DEBUG:
                if (logger.isDebugEnabled()) {
                    logger.debug(format(supplier.get(), formatObj));
                }
                break;
            case INFO:
                if (logger.isInfoEnabled()) {
                    logger.info(format(supplier.get(), formatObj));
                }
                break;
            case WARN:
                if (logger.isWarnEnabled()) {
                    logger.warn(format(supplier.get(), formatObj));
                }
                break;
            case ERROR:
                if (logger.isErrorEnabled()) {
                    logger.error(format(supplier.get(), formatObj));
                }
                break;
        }
    }

    public static void log(LogUtils level, Logger logger, Supplier<String> supplier, Supplier<Object[]> parms) {
        try {
            switch (level) {
                case DEBUG:
                    if (logger.isDebugEnabled()) {
                        logger.debug(format(supplier.get(), parms.get()));
                    }
                    break;
                case INFO:
                    if (logger.isInfoEnabled()) {
                        logger.info(format(supplier.get(), parms.get()));
                    }
                    break;
                case WARN:
                    if (logger.isWarnEnabled()) {
                        logger.warn(format(supplier.get(), parms.get()));
                    }
                    break;
                case ERROR:
                    if (logger.isErrorEnabled()) {
                        logger.error(format(supplier.get(), parms.get()));
                    }
                    break;
            }
        } catch (Exception e) {
            log(ERROR, logger, () -> getFullStackTrace(e));
        }
    }


    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        org.springframework.util.Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
        for (Method method : methods) {
            if (name.equals(method.getName()) &&
                    (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                return method;
            }
        }
        return null;
    }

    public static void main(String[] args) {
    }
}
