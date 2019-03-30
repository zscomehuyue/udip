package com.hwl.otter.clazz.mapping.cache.manager;

import java.util.List;

public interface CacheService<T extends CacheEntity> {

    T queryEntityById(Object id);

    T addToCache(T obj);

    int queryCount(String searchKey);

    List<T> queryPageList(String searchKey, int start, int size);

}
