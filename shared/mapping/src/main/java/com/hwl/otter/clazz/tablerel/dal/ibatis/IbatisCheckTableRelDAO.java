package com.hwl.otter.clazz.tablerel.dal.ibatis;

import com.hwl.otter.clazz.tablerel.dal.CheckTableRelDAO;
import com.hwl.otter.clazz.tablerel.dal.dataobject.CheckTableRelDo;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/21 16:36
 */
public class IbatisCheckTableRelDAO extends SqlMapClientDaoSupport implements CheckTableRelDAO{


    public CheckTableRelDo findCheckTableRelByTableName(String tableName){
        return (CheckTableRelDo) getSqlMapClientTemplate().queryForObject("findCheckTableRelByTableName", tableName);
    }


    public void insert(CheckTableRelDo checkTableRelDo){
        getSqlMapClientTemplate().insert("insertCheckTableRelDo", checkTableRelDo);
    }

    public void update(CheckTableRelDo checkTableRelDo){
        getSqlMapClientTemplate().update("updateCheckTableRelDoById", checkTableRelDo);
    }

    public int getCount(Map condition){
        return (Integer) getSqlMapClientTemplate().queryForObject("getCheckTableRelCount", condition);
    }

    public List<CheckTableRelDo> listCheckTableRel(Map condition){
        return getSqlMapClientTemplate().queryForList("listCheckTableRel",condition);
    }

    public void deleteCheckTableRel(Integer id){
        getSqlMapClientTemplate().delete("deleteCheckTableRel",id);
    }

    public CheckTableRelDo getCheckTableRelDoById(Object id) {
        return (CheckTableRelDo) getSqlMapClientTemplate().queryForObject("getCheckTableRelDoById", id);
    }

}
