package com.hwl.otter.clazz.datacheck.impl;

import com.hwl.otter.clazz.datacheck.DataCheckService;
import com.hwl.otter.clazz.datacheck.dal.DataCheckDAO;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/20 17:07
 */
public class DataCheckServiceImpl implements DataCheckService {

    private DataCheckDAO dataCheckDAO;

    @Override
    public void insertDataCheckDo(DataCheckDo dataCheckDo) {
        dataCheckDAO.insertDataCheckDo(dataCheckDo);
    }

    @Override
    public void updateDataCheckDoById(DataCheckDo dataCheckDo) {
        dataCheckDAO.updateDataCheckDoById(dataCheckDo);
    }


    @Override
    public List<DataCheckDo> findByCondition(DataCheckDo dataCheckDo) {
        return dataCheckDAO.findByCondition(dataCheckDo);
    }

    public DataCheckDAO getDataCheckDAO() {
        return dataCheckDAO;
    }

    public void setDataCheckDAO(DataCheckDAO dataCheckDAO) {
        this.dataCheckDAO = dataCheckDAO;
    }


    public int getCount(Map condition){
        return  dataCheckDAO.getCount(condition);
    }

    public List<DataCheckDo> listCheckDataLogRel(Map condition){
        return dataCheckDAO.listCheckDataLogRel(condition);
    }


    public void deleteCheckData(Integer id){
        dataCheckDAO.deleteCheckData(id);
    }



    public DataCheckDo getCheckDataLogById(Long id){
        return dataCheckDAO.getCheckDataLogById(id);
    }


    public List<String> getSourceSchemaList(){
        return dataCheckDAO.getSourceSchemaList();
    }

    public List<String> getTargetSchemaList(){
        return dataCheckDAO.getTargetSchemaList();
    }
}
