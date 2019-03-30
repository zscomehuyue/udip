package com.alibaba.otter.shared.common.utils.log;

import com.alibaba.otter.shared.common.utils.DateUtils;

public class LogMsg {
    private long start;
    private long end;
    private String className;
    private String methodName;
    private Object[] params;
    private String stack;
    private String logTime = DateUtils.nowStr();

    public void clear() {
        setStack(null);
        setClassName(null);
        setEnd(0);
        setLogTime(DateUtils.nowStr());
        setMethodName(null);
        setParams(null);
        setStart(0);
    }

    public String toMsg() {
        StringBuilder msg = new StringBuilder();
        msg.append("=exec ").append(this.getClassName()).append(".");
        msg.append(this.getMethodName());
        msg.append("=>");
        msg.append(" spend : ").append(this.getEnd() - this.getStart()).append(" milliseconds.");
        String values = msg.toString();
        return values;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }
}
