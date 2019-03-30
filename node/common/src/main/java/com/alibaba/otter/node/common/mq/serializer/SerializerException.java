package com.alibaba.otter.node.common.mq.serializer;

/**
 * @author zscome
 */
public class SerializerException extends RuntimeException {
    private static final long serialVersionUID = -948934144333391208L;

    public SerializerException() {
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerException(Throwable cause) {
        super(cause);
    }
}
