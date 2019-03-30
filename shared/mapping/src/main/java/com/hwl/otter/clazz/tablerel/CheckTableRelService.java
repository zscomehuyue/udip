package com.hwl.otter.clazz.tablerel;

import com.hwl.otter.clazz.tablerel.dal.dataobject.CheckTableRelDo;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/21 16:37
 */
public interface CheckTableRelService {


    void insert(CheckTableRelDo checkTableRelDo);

    void update(CheckTableRelDo checkTableRelDo);


    CheckTableRelDo findCheckTableRelByTableName(String tableName);


    int getCount(Map condition);


    List<CheckTableRelDo> listCheckTableRel(Map condition);


    void deleteCheckTableRel(Integer id);

    CheckTableRelDo getCheckTableRelDoById(Object id);
}
