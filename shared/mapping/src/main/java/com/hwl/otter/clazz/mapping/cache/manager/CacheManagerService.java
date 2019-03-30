package com.hwl.otter.clazz.mapping.cache.manager;

import java.util.List;

public interface CacheManagerService {

    boolean addToCacheById(Object id);

    boolean addToCacheByQuery(String searchKey);

    int getCountFromCache(String searchKey);

    List getPageListFromCache(String searchKey, int start, int size);

    void deleteAll();

}
