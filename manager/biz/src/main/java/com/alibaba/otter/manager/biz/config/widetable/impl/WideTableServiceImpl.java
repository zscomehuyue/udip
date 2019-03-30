package com.alibaba.otter.manager.biz.config.widetable.impl;

import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.widetable.WideTableService;
import com.alibaba.otter.manager.biz.config.widetable.dal.WideTableDAO;
import com.alibaba.otter.manager.biz.config.widetable.dal.dataobject.WideTableDO;
import com.alibaba.otter.shared.common.model.config.data.WideTable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WideTableServiceImpl implements WideTableService {

    private WideTableDAO wideTableDAO;

    private DataMediaService dataMediaService;

    @Override
    public void create(WideTable entityObj) {

    }

    public WideTableDO insert(WideTableDO entityObj) {
        if ("null".equals(entityObj.getSlaveMainTablePkIdName())) {
            entityObj.setSlaveMainTablePkIdName(null);
        }
        return wideTableDAO.insert(entityObj);
    }

    @Override
    public void remove(Long identity) {
        wideTableDAO.delete(identity);
    }

    @Override
    public void modify(WideTable entityObj) {

    }


    public void update(WideTableDO entityObj) {
        wideTableDAO.update(entityObj);
    }

    @Override
    public WideTable findById(Long identity) {
        WideTableDO wideTableDO = wideTableDAO.findById(identity);
        return doToModel(wideTableDO);
    }

    @Override
    public List<WideTable> listByIds(Long... identities) {
        return null;
    }

    @Override
    public List<WideTable> listAll() {
        return null;
    }

    @Override
    public List<WideTable> listByCondition(Map condition) {
        List<WideTableDO> wideTableDOList = wideTableDAO.listByCondition(condition);
        return doToModel(wideTableDOList);
    }

    @Override
    public int getCount() {
        return wideTableDAO.getCount();
    }

    @Override
    public int getCount(Map condition) {
        return wideTableDAO.getCount(condition);
    }

    private WideTable doToModel(WideTableDO wideTableDO) {
        WideTable table = new WideTable();
        table.setId(wideTableDO.getId());
        table.setTarget(dataMediaService.findById(wideTableDO.getTargetId()));
        table.setWideTableName(wideTableDO.getWideTableName());
        table.setMainTable(dataMediaService.findById(wideTableDO.getMainTableId()));
        table.setSlaveTable(dataMediaService.findById(wideTableDO.getSlaveTableId()));
        table.setMainTablePkIdName(wideTableDO.getMainTablePkIdName());
        table.setSlaveTablePkIdName(wideTableDO.getSlaveTablePkIdName());
        table.setMainTableFkIdName(wideTableDO.getMainTableFkIdName());
        table.setSlaveTableFkIdName(wideTableDO.getSlaveTableFkIdName());
        table.setSlaveMainTablePkIdName(wideTableDO.getSlaveMainTablePkIdName());
        table.setDescription(wideTableDO.getDescription());
        table.setCreated(wideTableDO.getCreated());
        table.setModified(wideTableDO.getModified());
        return table;

    }

    private List<WideTable> doToModel(List<WideTableDO> list) {
        return list.stream().map(wideTableDO -> doToModel(wideTableDO)).collect(Collectors.toList());

    }

    @Override
    public List<WideTable> listByTargetIdAndTableId(Long targetId, Long tableId) {
        return doToModel(wideTableDAO.listByTagetIdAndTableId(targetId, tableId));
    }

    public void setWideTableDAO(WideTableDAO wideTableDAO) {
        this.wideTableDAO = wideTableDAO;
    }

    public void setDataMediaService(DataMediaService dataMediaService) {
        this.dataMediaService = dataMediaService;
    }
}
