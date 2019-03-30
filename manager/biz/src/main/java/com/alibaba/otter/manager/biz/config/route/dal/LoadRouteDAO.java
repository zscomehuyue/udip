package com.alibaba.otter.manager.biz.config.route.dal;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.route.dal.dataobject.LoadRouteDO;

import java.util.List;

public interface LoadRouteDAO extends GenericDAO<LoadRouteDO> {
    List<LoadRouteDO> listByPipelineId(Long pipelineId);

    void deleteByPipelineIdAndTableId(LoadRouteDO entity);
}
