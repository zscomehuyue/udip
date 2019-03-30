package com.hwl.otter.clazz.tablerel.impl;

import com.hwl.otter.clazz.CacheNameConstants;
import com.hwl.otter.clazz.tablerel.CheckTableRelService;
import com.hwl.otter.clazz.tablerel.dal.CheckTableRelDAO;
import com.hwl.otter.clazz.tablerel.dal.dataobject.CheckTableRelDo;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/21 16:37
 */
public class CheckTableRelServiceImpl implements CheckTableRelService {

    private CheckTableRelDAO checkTableRelDAO;


    public void insert(CheckTableRelDo checkTableRelDo) {
        checkTableRelDAO.insert(checkTableRelDo);
    }

    public void update(CheckTableRelDo checkTableRelDo) {
        checkTableRelDAO.update(checkTableRelDo);
    }

    @Cacheable(value = CacheNameConstants.DB_REPAIR_CACHE_NAME, key = "#tableName")
    public CheckTableRelDo findCheckTableRelByTableName(String tableName) {
        return checkTableRelDAO.findCheckTableRelByTableName(tableName);
    }

    public int getCount(Map condition) {
        return checkTableRelDAO.getCount(condition);
    }

    public List<CheckTableRelDo> listCheckTableRel(Map condition) {
        return checkTableRelDAO.listCheckTableRel(condition);
    }


    public void deleteCheckTableRel(Integer id) {
        checkTableRelDAO.deleteCheckTableRel(id);
    }


    public CheckTableRelDo getCheckTableRelDoById(Object id) {
        return checkTableRelDAO.getCheckTableRelDoById(id);
    }


    public CheckTableRelDAO getCheckTableRelDAO() {
        return checkTableRelDAO;
    }

    public void setCheckTableRelDAO(CheckTableRelDAO checkTableRelDAO) {
        this.checkTableRelDAO = checkTableRelDAO;
    }
}
