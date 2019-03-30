package com.hwl.otter.clazz.repairlog.impl;

import com.hwl.otter.clazz.repairlog.CheckRepairLogService;
import com.hwl.otter.clazz.repairlog.dal.CheckRepairLogDAO;
import com.hwl.otter.clazz.repairlog.dal.dataobject.CheckRepairLogDo;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/22 11:13
 */
public class CheckRepairLogServiceImpl implements CheckRepairLogService{

    private CheckRepairLogDAO checkRepairLogDAO;


    public void insertcheckRepairLogDo(CheckRepairLogDo checkRepairLogDo){
        checkRepairLogDAO.insertcheckRepairLogDo(checkRepairLogDo);
    }

    public List<CheckRepairLogDo> findCheckRepairLogDoByCondition(CheckRepairLogDo checkRepairLogDo){
        return checkRepairLogDAO.findCheckRepairLogDoByCondition(checkRepairLogDo);
    }


    public int getCount(Map condition){
        return checkRepairLogDAO.getCount(condition);
    }

    public List<CheckRepairLogDo> listCheckTableRel(Map condition){
        return checkRepairLogDAO.listCheckRepairLogRel(condition);
    }

    public List<CheckRepairLogDo> getRepairFailData(){
        return checkRepairLogDAO.getRepairFailData();
    }


    public CheckRepairLogDo findById(Long id){
        return checkRepairLogDAO.findById(id);
    }


    public void updatecheckRepairLogDoById(CheckRepairLogDo checkRepairLogDo){
        checkRepairLogDAO.updatecheckRepairLogDoById(checkRepairLogDo);
    }

    public void updatecheckRepairLogDoByCondition(CheckRepairLogDo checkRepairLogDo){
        checkRepairLogDAO.updatecheckRepairLogDoByCondition(checkRepairLogDo);
    }


    public void deleteById(Long id){
        checkRepairLogDAO.deleteById(id);
    }


    public CheckRepairLogDAO getCheckRepairLogDAO() {
        return checkRepairLogDAO;
    }

    public void setCheckRepairLogDAO(CheckRepairLogDAO checkRepairLogDAO) {
        this.checkRepairLogDAO = checkRepairLogDAO;
    }


    public List<String> getSourceSchemaList(){
        return checkRepairLogDAO.getSourceSchemaList();
    }

    public List<String> getSourceTableList(){
        return checkRepairLogDAO.getSourceTableList();
    }

}
