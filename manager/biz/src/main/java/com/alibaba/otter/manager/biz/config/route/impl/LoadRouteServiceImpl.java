package com.alibaba.otter.manager.biz.config.route.impl;

import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.config.route.LoadRouteService;
import com.alibaba.otter.manager.biz.config.route.dal.LoadRouteDAO;
import com.alibaba.otter.manager.biz.config.route.dal.dataobject.LoadRouteDO;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.LoadRoute;
import com.alibaba.otter.shared.common.model.config.data.LoadType;
import com.alibaba.otter.shared.common.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadRouteServiceImpl implements LoadRouteService {
    private static final Logger logger = LoggerFactory.getLogger(LoadRouteServiceImpl.class);

    private PipelineService pipelineService;
    private DataMediaService dataMediaService;
    private LoadRouteDAO loadRouteDAO;


    @Override
    public void create(LoadRoute entityObj) {

    }

    @Override
    public void createRoute(Long pipelineId, int isRouteData, int isLoadWideTableES,
                            DataMedia targetDataMedia, DataMedia targetEsDataMedia, DataMedia targetMqRabbitDataMedia) {
        LoadRouteDO data = null;
        LoadRouteDO esData = null;
        LoadRouteDO rabbitData = null;

        if (targetDataMedia.getId() != 0) {
            if (isRouteData == 0) {
                data = new LoadRouteDO();
                data.setPipelineId(pipelineId);
                data.setTableId(targetDataMedia.getId());
                data.setLoadDataMediaId(targetDataMedia.getId());
                data.setType(isLoadWideTableES);
            }
        }

        if (targetEsDataMedia.getId() != 0) {
            esData = new LoadRouteDO();
            esData.setPipelineId(pipelineId);
            esData.setTableId(targetDataMedia.getId());
            esData.setLoadDataMediaId(targetEsDataMedia.getId());
            esData.setType(isLoadWideTableES);
        }

        if (targetMqRabbitDataMedia.getId() != 0) {
            rabbitData = new LoadRouteDO();
            rabbitData.setPipelineId(pipelineId);
            rabbitData.setTableId(targetDataMedia.getId());
            rabbitData.setLoadDataMediaId(targetMqRabbitDataMedia.getId());
            rabbitData.setType(isLoadWideTableES);
        }

        if (data != null) {
            this.insert(data);
        }
        if (esData != null) {
            this.insert(esData);
        }
        if (rabbitData != null) {
            this.insert(rabbitData);
        }

    }


    public void editRoute(Long pipelineId, Long tableId, int isRouteData, int isLoadWideTableES,
                          DataMedia targetDataMedia, DataMedia targetEsDataMedia, DataMedia targetMqRabbitDataMedia) {
        LoadRouteDO loadRouteDO = new LoadRouteDO();
        loadRouteDO.setPipelineId(pipelineId);
        loadRouteDO.setTableId(tableId);
        this.deleteByPipelineIdAndTableId(loadRouteDO);
        this.createRoute(pipelineId, isRouteData, isLoadWideTableES, targetDataMedia, targetEsDataMedia, targetMqRabbitDataMedia);
    }

    public LoadRouteDO insert(LoadRouteDO entityObj) {
        return loadRouteDAO.insert(entityObj);
    }

    @Override
    public void remove(Long identity) {

    }

    @Override
    public void modify(LoadRoute entityObj) {

    }

    @Override
    public LoadRoute findById(Long identity) {
        return null;
    }

    @Override
    public List<LoadRoute> listByIds(Long... identities) {
        return null;
    }

    @Override
    public List<LoadRoute> listAll() {
        return null;
    }

    @Override
    public List<LoadRoute> listByCondition(Map condition) {
        List<LoadRouteDO> loadRouteDOList = loadRouteDAO.listByCondition(condition);
        return doToModel(loadRouteDOList);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getCount(Map condition) {
        return 0;
    }

    public void setLoadRouteDAO(LoadRouteDAO loadRouteDAO) {
        this.loadRouteDAO = loadRouteDAO;
    }

    public LoadRoute doToModel(LoadRouteDO loadRouteDO) {
        LoadRoute route = new LoadRoute();
        route.setId(loadRouteDO.getId());
        route.setPipelineId(loadRouteDO.getPipelineId());
        route.setTable(dataMediaService.findById(loadRouteDO.getTableId()));
        route.setLoadDataMedia(dataMediaService.findById(loadRouteDO.getLoadDataMediaId()));
        route.setType(LoadType.valueOf(loadRouteDO.getType()));
        route.setDescription(loadRouteDO.getDescription());
        route.setCreated(loadRouteDO.getCreated());
        route.setModified(loadRouteDO.getModified());
        return route;

    }

    private List<LoadRoute> doToModel(List<LoadRouteDO> routeDOS) {
        List<LoadRoute> routes = new ArrayList<LoadRoute>();
        for (LoadRouteDO routeDO : routeDOS) {
            routes.add(doToModel(routeDO));
        }
        return routes;
    }


    @Override
    public List<LoadRoute> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<LoadRoute> routes = new ArrayList<LoadRoute>();
        List<LoadRouteDO> list = loadRouteDAO.listByPipelineId(pipelineId);
        if (CollectionUtils.isEmpty(list)) {
            return routes;
        }
        routes = doToModel(list);
        return routes;
    }


    public void deleteByPipelineIdAndTableId(LoadRouteDO entity) {
        loadRouteDAO.deleteByPipelineIdAndTableId(entity);
    }

    public void setPipelineService(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public void setDataMediaService(DataMediaService dataMediaService) {
        this.dataMediaService = dataMediaService;
    }
}
