package com.hwl.otter.clazz.datacheck;

import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/20 17:07
 */
public interface DataCheckService {

    void insertDataCheckDo(DataCheckDo dataCheckDo);

    void updateDataCheckDoById(DataCheckDo dataCheckDo);


    List<DataCheckDo> findByCondition(DataCheckDo dataCheckDo);

    int getCount(Map condition);

    List<DataCheckDo> listCheckDataLogRel(Map condition);

    void deleteCheckData(Integer id);

    DataCheckDo getCheckDataLogById(Long id);

    List<String> getSourceSchemaList();

    List<String> getTargetSchemaList();
}
