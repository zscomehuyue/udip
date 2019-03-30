package com.alibaba.otter.manager.biz.config.widetable;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.manager.biz.config.widetable.dal.dataobject.WideTableDO;
import com.alibaba.otter.shared.common.model.config.data.WideTable;

import java.util.List;

public interface WideTableService extends GenericService<WideTable> {

    List<WideTable> listByTargetIdAndTableId(Long targetId, Long tableId);


    WideTableDO insert(WideTableDO entityObj);


    void update(WideTableDO entityObj);

}
