package com.hwl.otter.clazz.repairlog.dal;

import com.hwl.otter.clazz.repairlog.dal.dataobject.CheckRepairLogDo;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/22 11:12
 */
public interface CheckRepairLogDAO {

    void insertcheckRepairLogDo(CheckRepairLogDo checkRepairLogDo);

    List<CheckRepairLogDo> findCheckRepairLogDoByCondition(CheckRepairLogDo checkRepairLogDo);

    List<CheckRepairLogDo> getRepairFailData();


    int getCount(Map condition);

    List<CheckRepairLogDo> listCheckRepairLogRel(Map condition);


    CheckRepairLogDo findById(Long id);


    void updatecheckRepairLogDoById(CheckRepairLogDo checkRepairLogDo);

    void updatecheckRepairLogDoByCondition(CheckRepairLogDo checkRepairLogDo);


    List<String> getSourceSchemaList();

    List<String> getSourceTableList();

    void deleteById(Long id);

}
