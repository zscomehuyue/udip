package com.alibaba.otter.shared.common.model.config.data.es;

import com.alibaba.otter.shared.common.model.config.data.DataMedia;

public class IndexDataMedia extends DataMedia<IndexMediaSource> {
    private static final long serialVersionUID = -3707123038412438229L;

    public String getIndex() {
        return getNamespace();
    }

    public void setIndex(String index) {
        setNamespace(index);
    }

    public String getIndexType() {
        return getName();
    }

    public void setIndexType(String indexType) {
        setName(indexType);
    }
}
