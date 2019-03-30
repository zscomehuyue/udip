package com.hwl.otter.clazz.mapping.cache.manager;

import com.alibaba.citrus.util.Paginator;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

public class CacheManagerServiceImpl implements CacheManagerService {
    private static final Logger logger = LoggerFactory.getLogger(CacheManagerServiceImpl.class);

    public static final int INIT_REDIS_PAGE_SIZE = 1000;
    public static final int FIRST_PAGE_INDEX = 1;
    public static final int SECOND_PAGE_INDEX = 2;
    private RedisTemplate redisTemplate;
    private CacheService cacheService;


    public boolean addToCacheById(Object id) {
        logger.info("=addToCacheById=>id=" + id);
        CacheEntity entity = cacheService.queryEntityById(id);
        cacheService.addToCache(entity);
        return true;
    }

    private boolean addToCache(List<CacheEntity> list) {
        for (CacheEntity object : list) {
            cacheService.addToCache(object);
        }
        return true;
    }

    @Override
    public boolean addToCacheByQuery(String searchKey) {
        int count = cacheService.queryCount(searchKey);
        Paginator paginator = new Paginator();
        paginator.setItems(count);
        paginator.setPage(FIRST_PAGE_INDEX);
        paginator.setItemsPerPage(INIT_REDIS_PAGE_SIZE);
        List list = cacheService.queryPageList(searchKey, paginator.getPage(), paginator.getItemsPerPage());
        addToCache(list);
        for (int i = SECOND_PAGE_INDEX; i <= paginator.getPages(); i++) {
            paginator.setPage(i);
            list = cacheService.queryPageList(searchKey, paginator.getPage(), paginator.getItemsPerPage());
            addToCache(list);
        }
        return true;
    }


    private List<CacheEntity> getMappingDoByKeys(final List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }


    @Override
    public int getCountFromCache(String searchKey) {
        return cacheService.queryCount(searchKey);
    }

    @Override
    public List getPageListFromCache(String searchKey, int start, int size) {
        List<CacheEntity> list = cacheService.queryPageList(searchKey, start, size);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> keys = new ArrayList<String>();
            for (CacheEntity mdo : list) {
                keys.add(mdo.getCacheName() + ":" + mdo.getCacheKey());
            }
            List<CacheEntity> redisList = getMappingDoByKeys(keys);
            redisList.removeAll(Collections.singleton(null));
            ((ArrayList) redisList).trimToSize();
            if (list.size() != redisList.size()) {
                list.removeAll(redisList);
                for (CacheEntity mdo : list) {
                    mdo.clear();
                }
                redisList.addAll(list);
                list = redisList;
                Collections.sort(list, new Comparator<CacheEntity>() {
                    @Override
                    public int compare(CacheEntity o1, CacheEntity o2) {
                        return o2.getId() - o1.getId();
                    }
                });
            }
        }
        return list;
    }



    public void deleteAll(){
        redisTemplate.delete(redisTemplate.keys("schoolCode*"));
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
}
