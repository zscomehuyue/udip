package com.alibaba.otter.node.etl.load.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class IndexLoadException extends NestableRuntimeException {

    private static final long serialVersionUID = 2680820522662343759L;
    private String errorCode;
    private String errorDesc;

    public IndexLoadException(String errorCode) {
        super(errorCode);
    }

    public IndexLoadException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public IndexLoadException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public IndexLoadException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    public IndexLoadException(Throwable cause) {
        super(cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
