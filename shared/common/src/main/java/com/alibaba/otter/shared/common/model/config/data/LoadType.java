package com.alibaba.otter.shared.common.model.config.data;

import java.util.Arrays;

public enum LoadType {

    SINGLE_INDEX(1), WIDE_SINGLE_INDEX(2);
    private int type;

    LoadType(int type) {
        this.type = type;
    }

    public boolean isLoadSingleIndex() {
        return this.equals(LoadType.SINGLE_INDEX);
    }

    public static LoadType valueOf(int type) {
        return Arrays.stream(values()).filter(loadType -> loadType.getType() == type).findFirst().orElse(null);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
