package com.alibaba.otter.manager.biz.config.widetable.dal.ibatis;

import com.alibaba.otter.manager.biz.config.widetable.dal.WideTableDAO;
import com.alibaba.otter.manager.biz.config.widetable.dal.dataobject.WideTableDO;
import com.alibaba.otter.shared.common.utils.Assert;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IbatisWideTableDAO extends SqlMapClientDaoSupport implements WideTableDAO {


    public WideTableDO insert(WideTableDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().insert("insertWideTable", entityObj);
        return entityObj;
    }

    public void delete(Long identity) {
        Assert.assertNotNull(identity);
        getSqlMapClientTemplate().delete("deleteWideTableById", identity);

    }

    public void update(WideTableDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().update("updateWideTable", entityObj);

    }

    public List<WideTableDO> listAll() {
        return (List<WideTableDO>) getSqlMapClientTemplate().queryForList("listWideTables");
    }

    public List<WideTableDO> listByCondition(Map condition) {

        return (List<WideTableDO>) getSqlMapClientTemplate().queryForList("listWideTablesWithCondition", condition);
    }

    public List<WideTableDO> listByMultiId(Long... identities) {
        return null;
    }

    public WideTableDO findById(Long identity) {
        Assert.assertNotNull(identity);
        return (WideTableDO) getSqlMapClientTemplate().queryForObject("findWideTableById", identity);
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getWideTableCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getWideTableCount", condition);
        return count.intValue();
    }

    public boolean checkUnique(WideTableDO entityObj) {
        return false;
    }

    public List<WideTableDO> listByTagetIdAndTableId(Long targetId, Long tableId) {
        Map map = new HashMap(2);
        map.put("targetId", targetId);
        map.put("tableId", tableId);
        return (List<WideTableDO>) getSqlMapClientTemplate().queryForList("listByTargetIdAndTableId", map);
    }


}
