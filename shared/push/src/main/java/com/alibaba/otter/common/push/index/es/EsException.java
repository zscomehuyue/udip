package com.alibaba.otter.common.push.index.es;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class EsException extends NestableRuntimeException {
    private static final long serialVersionUID = 2680820522662343759L;
    private String errorCode;
    private String errorDesc;

    public EsException(String errorCode) {
        super(errorCode);
    }

    public EsException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public EsException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public EsException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }

    public EsException(Throwable cause) {
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
