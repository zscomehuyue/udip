package com.alibaba.otter.common.push.index.wide.event;

public enum ExtEventType {
    //TODO 增加1个字段排序；
    ALARM("错误告警", 2),
    ALARM_WARN("错误告警", 3),
    UNKNOWN("unknown", 0);
    private String name;
    private Integer value;

    ExtEventType(String name, Integer value) {
        this.value = value;
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public static ExtEventType valueOf(Integer value) {
        if (null == value) {
            return UNKNOWN;
        }
        for (ExtEventType type : values()) {
            if (value.equals(type.getValue())) {
                return type;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "ExtEventType{" +
                "value=" + value +
                ", name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

}
