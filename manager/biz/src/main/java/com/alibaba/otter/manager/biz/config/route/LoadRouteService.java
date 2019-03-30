package com.alibaba.otter.manager.biz.config.route;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.manager.biz.config.route.dal.dataobject.LoadRouteDO;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.LoadRoute;

import java.util.List;

public interface LoadRouteService extends GenericService<LoadRoute> {

    List<LoadRoute> listByPipelineId(Long pipelineId);


    void createRoute(Long pipelineId, int isRouteData,int isLoadWideTableES,
                     DataMedia targetDataMedia, DataMedia targetEsDataMedia,DataMedia targetMqRabbitDataMedia);

    void editRoute(Long pipelineId,Long tableId,int isRouteData,int isLoadWideTableES,
                   DataMedia targetDataMedia, DataMedia targetEsDataMedia,DataMedia targetMqRabbitDataMedia);

    void deleteByPipelineIdAndTableId(LoadRouteDO entity);
}
