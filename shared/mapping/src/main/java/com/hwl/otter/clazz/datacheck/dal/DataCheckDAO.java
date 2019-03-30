package com.hwl.otter.clazz.datacheck.dal;

import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/20 17:05
 */
public interface DataCheckDAO {

    void insertDataCheckDo(DataCheckDo dataCheckDo);

    void updateDataCheckDoById(DataCheckDo dataCheckDo);


    void deleteCheckData(Integer id);

    List<DataCheckDo> findByCondition(DataCheckDo dataCheckDo);

    int getCount(Map condition);

    List<DataCheckDo> listCheckDataLogRel(Map condition);

    DataCheckDo getCheckDataLogById(Long id);


    List<String> getSourceSchemaList();

    List<String> getTargetSchemaList();

}
