package com.hwl.otter.clazz.mapping.cache.manager;

public interface CacheEntity {
    String getCacheName();

    String getCacheKey();

    void clear();

    boolean equals(Object o);

    int hashCode();

    Integer getId();
}
