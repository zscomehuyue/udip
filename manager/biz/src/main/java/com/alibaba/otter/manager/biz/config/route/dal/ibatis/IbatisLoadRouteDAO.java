package com.alibaba.otter.manager.biz.config.route.dal.ibatis;

import com.alibaba.otter.manager.biz.config.route.dal.LoadRouteDAO;
import com.alibaba.otter.manager.biz.config.route.dal.dataobject.LoadRouteDO;
import com.alibaba.otter.shared.common.utils.Assert;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import java.util.List;
import java.util.Map;

public class IbatisLoadRouteDAO extends SqlMapClientDaoSupport implements LoadRouteDAO {


    public LoadRouteDO insert(LoadRouteDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().insert("insertLoadRoute", entityObj);
        return entityObj;
    }

    public void delete(Long identity) {
        Assert.assertNotNull(identity);
        getSqlMapClientTemplate().delete("deleteLoadRouteById", identity);

    }

    public void update(LoadRouteDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlMapClientTemplate().update("updateLoadRoute", entityObj);

    }

    public List<LoadRouteDO> listAll() {
        List<LoadRouteDO> LoadRouteDOs = getSqlMapClientTemplate().queryForList("listLoadRoutes");
        return LoadRouteDOs;
    }

    public List<LoadRouteDO> listByCondition(Map condition) {

        List<LoadRouteDO> LoadRouteDOs = getSqlMapClientTemplate().queryForList("listLoadRoutesWithCondition",
                condition);
        return LoadRouteDOs;
    }

    public List<LoadRouteDO> listByMultiId(Long... identities) {
        return null;
    }

    public LoadRouteDO findById(Long identity) {
        Assert.assertNotNull(identity);
        return (LoadRouteDO) getSqlMapClientTemplate().queryForObject("findLoadRouteById", identity);
    }

    public int getCount() {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getLoadRouteCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlMapClientTemplate().queryForObject("getLoadRouteCountWithPIdAndSearchKey",
                condition);
        return count.intValue();
    }

    public boolean checkUnique(LoadRouteDO entityObj) {
        return false;
    }

    public List<LoadRouteDO> listByPipelineId(Long pipelineId) {
        List<LoadRouteDO> LoadRouteDOs = getSqlMapClientTemplate().queryForList("listLoadRoutesByPipelineId",
                pipelineId);
        return LoadRouteDOs;
    }

    public List<LoadRouteDO> listByPipelineIdWithoutContent(Long pipelineId) {
        List<LoadRouteDO> LoadRouteDOs = getSqlMapClientTemplate().queryForList("listLoadRoutesByPipelineIdWithoutContent",
                pipelineId);
        return LoadRouteDOs;
    }

    public void deleteByPipelineIdAndTableId(LoadRouteDO entity){
        getSqlMapClientTemplate().delete("deleteByPipelineIdAndTableId",entity);
    }
}
