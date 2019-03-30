package com.hwl.otter.clazz.repairlog.dal.ibatis;

import com.hwl.otter.clazz.repairlog.dal.CheckRepairLogDAO;
import com.hwl.otter.clazz.repairlog.dal.dataobject.CheckRepairLogDo;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/22 11:13
 */
public class IbatisCheckRepairLogDAO extends SqlMapClientDaoSupport implements CheckRepairLogDAO{

    public void insertcheckRepairLogDo(CheckRepairLogDo checkRepairLogDo){
        getSqlMapClientTemplate().insert("insertcheckRepairLogDo", checkRepairLogDo);
    }

    public void updatecheckRepairLogDoById(CheckRepairLogDo checkRepairLogDo){
        getSqlMapClientTemplate().update("updatecheckRepairLogDoById", checkRepairLogDo);
    }

    public void updatecheckRepairLogDoByCondition(CheckRepairLogDo checkRepairLogDo){
        getSqlMapClientTemplate().update("updatecheckRepairLogDoByCondition", checkRepairLogDo);
    }

    public List<CheckRepairLogDo> findCheckRepairLogDoByCondition(CheckRepairLogDo checkRepairLogDo){
        return (List<CheckRepairLogDo> ) getSqlMapClientTemplate().queryForList("findCheckRepairLogDoByCondition", checkRepairLogDo);
    }


    public List<CheckRepairLogDo> getRepairFailData(){
        return (List<CheckRepairLogDo> ) getSqlMapClientTemplate().queryForList("getRepairFailData");
    }

    public int getCount(Map condition){
        return (Integer) getSqlMapClientTemplate().queryForObject("getCheckRepairLogCount", condition);
    }

    public List<CheckRepairLogDo> listCheckRepairLogRel(Map condition){
        return getSqlMapClientTemplate().queryForList("listCheckRepairLog",condition);
    }


    public CheckRepairLogDo findById(Long id){
        return (CheckRepairLogDo)getSqlMapClientTemplate().queryForObject("findCheckRepairLogById",id);
    }

    public void deleteById(Long id){
        getSqlMapClientTemplate().delete("deleteRepairLog",id);
    }


    public List<String> getSourceSchemaList(){
        return getSqlMapClientTemplate().queryForList("getSourceSchemaList");
    }

    public List<String> getSourceTableList(){
        return getSqlMapClientTemplate().queryForList("getSourceTableList");
    }

}
