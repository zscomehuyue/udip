package com.alibaba.otter.manager.biz.config.widetable.dal;

import com.alibaba.otter.manager.biz.common.basedao.GenericDAO;
import com.alibaba.otter.manager.biz.config.widetable.dal.dataobject.WideTableDO;

import java.util.List;

public interface WideTableDAO extends GenericDAO<WideTableDO> {
    List<WideTableDO> listByTagetIdAndTableId(Long targetId, Long tableId);
}
