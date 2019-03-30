package com.alibaba.otter.manager.biz.check;

import java.util.Arrays;

public enum OperateEnum {
    findAndRepair("subRepair"), timer("timer"), Repair("repair"), checkFieldAndRepair("checkFieldAndRepair"),forceRepair("forceRepair"), unknown("unknown");
    private String name;

    OperateEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static OperateEnum valueOfName(String name) {
        OperateEnum operateEnum = Arrays.stream(values()).filter(type -> type.getName().equals(name)).findFirst().orElse(OperateEnum.unknown);
        return operateEnum;
    }

    public static boolean contains(String name) {
        OperateEnum type = valueOfName(name);
        return null != type && type != unknown;
    }
}
