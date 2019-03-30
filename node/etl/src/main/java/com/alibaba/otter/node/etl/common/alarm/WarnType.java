package com.alibaba.otter.node.etl.common.alarm;

public enum WarnType {
    DINGDING("钉钉", 1), mail("邮件", 2), message("短信", 3), UNKNOWN("unknown", -1);
    private String name;
    private Integer value;

    WarnType(String name, Integer value) {
        this.value = value;
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static WarnType valueOf(Integer value) {
        if (null == value) {
            return UNKNOWN;
        }
        for (WarnType type : values()) {
            if (value.equals(type.getValue())) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
