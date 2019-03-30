package com.hwl.otter.clazz.datacheck.dal.ibatis;

import com.hwl.otter.clazz.datacheck.dal.DataCheckDAO;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/20 17:05
 */
public class IbatisDataCheckDAO extends SqlMapClientDaoSupport implements DataCheckDAO {

    @Override
    public void insertDataCheckDo(DataCheckDo dataCheckDo) {
        getSqlMapClientTemplate().insert("insertDataCheckDo", dataCheckDo);
    }

    @Override
    public void updateDataCheckDoById(DataCheckDo dataCheckDo) {
        getSqlMapClientTemplate().update("updateDataCheckDoById", dataCheckDo);
    }


    @Override
    public List<DataCheckDo> findByCondition(DataCheckDo dataCheckDo) {
        return (List<DataCheckDo>) getSqlMapClientTemplate().queryForList("findDataCheckByCondition", dataCheckDo);
    }


    public int getCount(Map condition){
        return (Integer) getSqlMapClientTemplate().queryForObject("getCheckDataLogCount", condition);
    }

    public List<DataCheckDo> listCheckDataLogRel(Map condition){
        return getSqlMapClientTemplate().queryForList("listCheckDataLog",condition);
    }


    public void deleteCheckData(Integer id){
        getSqlMapClientTemplate().delete("deleteCheckData",id);
    }


    public DataCheckDo getCheckDataLogById(Long id){
        return (DataCheckDo) getSqlMapClientTemplate().queryForObject("getCheckDataLogById",id);
    }


    public List<String> getSourceSchemaList(){
        return  getSqlMapClientTemplate().queryForList("getCheckSourceSchemaList");
    }
    public List<String> getTargetSchemaList(){
        return  getSqlMapClientTemplate().queryForList("getCheckTargetSchemaList");
    }
}
