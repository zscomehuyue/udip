package com.alibaba.otter.common.push.index.type;

public enum OperateType {
    DELETE("DELETE"), INSERT("INSERT"), UPDATE("UPDATE"), IGNORE("IGNORE");
    private String name;

    OperateType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public boolean isUpdate() {
        return this.equals(OperateType.UPDATE);
    }

    public boolean isInsert() {
        return this.equals(OperateType.INSERT);
    }

    public boolean isDel() {
        return this.equals(OperateType.DELETE);
    }
}