/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.manager.web.home.module.action;

import com.alibaba.citrus.service.form.CustomErrors;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.ManagerException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.biz.config.route.LoadRouteService;
import com.alibaba.otter.manager.biz.config.route.dal.dataobject.LoadRouteDO;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.*;
import com.alibaba.otter.shared.common.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.otter.shared.common.utils.LogUtils.ERROR;

public class DataMediaPairAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;

    @Resource(name = "dataMediaService")
    private DataMediaService dataMediaService;

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    @Resource(name = "channelService")
    private ChannelService channelService;

    @Resource(name = "loadRouteService")
    private LoadRouteService loadRouteService;

    /**
     * 添加DataMediaPair
     *
     * @throws Exception
     */
    public void doAdd(@Param("submitKey") String submitKey, @FormGroup("dataMediaPairInfo") Group dataMediaPairInfo,
                      @FormField(name = "formDataMediaPairError", group = "dataMediaPairInfo") CustomErrors err,
                      Navigator nav) throws Exception {
        DataMediaPair dataMediaPair = new DataMediaPair();
        DataMedia sourceDataMedia = new DataMedia();
        DataMedia targetDataMedia = new DataMedia();
        DataMedia targetEsDataMedia = new DataMedia();
        DataMedia targetMqRabbitDataMedia = new DataMedia();
        dataMediaPairInfo.setProperties(dataMediaPair);

        int isRouteData = dataMediaPairInfo.getField("isRouteData").getIntegerValue();
        int isLoadWideTableES = dataMediaPairInfo.getField("isLoadWideTableES").getIntegerValue();

        targetEsDataMedia.setId(dataMediaPairInfo.getField("targetEsDataMediaId").getLongValue());
        targetMqRabbitDataMedia.setId(dataMediaPairInfo.getField("targetMqRabbitDataMediaId").getLongValue());


        // filter解析
        ExtensionDataType filterType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("filterType").getStringValue());
        ExtensionData filterData = new ExtensionData();
        filterData.setExtensionDataType(filterType);
        if (filterType.isClazz()) {
            filterData.setClazzPath(dataMediaPairInfo.getField("filterText").getStringValue());
        } else if (filterType.isSource()) {
            filterData.setSourceText(dataMediaPairInfo.getField("filterText").getStringValue());
        }
        dataMediaPair.setFilterData(filterData);

        // fileresovler解析
        ExtensionDataType resolverType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("resolverType").getStringValue());
        ExtensionData resolverData = new ExtensionData();
        resolverData.setExtensionDataType(resolverType);
        if (resolverType.isClazz()) {
            resolverData.setClazzPath(dataMediaPairInfo.getField("resolverText").getStringValue());
        } else if (resolverType.isSource()) {
            resolverData.setSourceText(dataMediaPairInfo.getField("resolverText").getStringValue());
        }
        dataMediaPair.setResolverData(resolverData);

        sourceDataMedia.setId(dataMediaPairInfo.getField("sourceDataMediaId").getLongValue());
        dataMediaPair.setSource(sourceDataMedia);

        targetDataMedia.setId(dataMediaPairInfo.getField("targetDataMediaId").getLongValue());
        dataMediaPair.setTarget(targetDataMedia);
        Long id = 0L;
        try {
            id = dataMediaPairService.createAndReturnId(dataMediaPair);
            // load route
            loadRouteService.createRoute(dataMediaPair.getPipelineId(), isRouteData, isLoadWideTableES, targetDataMedia, targetEsDataMedia, targetMqRabbitDataMedia);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaPair");
            return;
        }
        if (submitKey.equals("保存")) {
            nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + dataMediaPair.getPipelineId());
        } else if (submitKey.equals("下一步")) {
            nav.redirectToLocation("addColumnPair.htm?dataMediaPairId=" + id + "&pipelineId="
                    + dataMediaPair.getPipelineId() + "&dataMediaPairId=" + id + "&sourceMediaId="
                    + sourceDataMedia.getId() + "&targetMediaId=" + targetDataMedia.getId());
        }
    }


    /**
     * 批量添加DataMediaPair
     *
     * @param
     * @throws Exception
     */
    public void doBatchAdd(@FormGroup("batchDataMediaPairInfo") Group batchDataMediaPairInfo,
                           @Param("pipelineId") Long pipelineId,
                           @FormField(name = "formBatchDataMediaPairError", group = "batchDataMediaPairInfo") CustomErrors err,
                           Navigator nav) throws Exception {
        String batchPairContent = batchDataMediaPairInfo.getField("batchPairContent").getStringValue();
        List<String> StringPairs = Arrays.asList(batchPairContent.split("\r\n"));
        try {
            for (String stringPair : StringPairs) {
                List<String> pairData = Arrays.asList(stringPair.split(","));
                if (pairData.size() < 7) {
                    throw new ManagerException("[" + stringPair + "] the line not all parameters");
                }
                Long sourceDataMediaId = Long.valueOf(pairData.get(0));
                Long targetDataMediaId = Long.valueOf(pairData.get(1));
                Long targetEsDataMediaId = Long.valueOf(pairData.get(2));
                String classPath = pairData.get(3);
                int isRouteData = Integer.valueOf(pairData.get(4));
                int isLoadWideTableES = Integer.valueOf(pairData.get(5));
                Long weight = Long.valueOf(pairData.get(6));
                Long targetMqRabbitDataMediaId = 0L;
                DataMediaPair dataMediaPair = new DataMediaPair();
                DataMedia sourceDataMedia = new DataMedia();
                DataMedia targetDataMedia = new DataMedia();
                DataMedia targetEsDataMedia = new DataMedia();
                DataMedia targetMqRabbitDataMedia = new DataMedia();
                dataMediaPair.setColumnPairMode(ColumnPairMode.INCLUDE);

                dataMediaPair.setPipelineId(pipelineId);
                dataMediaPair.setPushWeight(weight);

                targetEsDataMedia.setId(targetEsDataMediaId);
                targetMqRabbitDataMedia.setId(targetMqRabbitDataMediaId);


                // filter解析
                ExtensionData filterData = new ExtensionData();
                filterData.setExtensionDataType(ExtensionDataType.CLAZZ);
                filterData.setClazzPath(classPath);
                dataMediaPair.setFilterData(filterData);

                ExtensionData resolverData = new ExtensionData();
                resolverData.setExtensionDataType(ExtensionDataType.CLAZZ);
                resolverData.setClazzPath(null);
                dataMediaPair.setResolverData(resolverData);

                sourceDataMedia.setId(sourceDataMediaId);
                dataMediaPair.setSource(sourceDataMedia);

                targetDataMedia.setId(targetDataMediaId);
                dataMediaPair.setTarget(targetDataMedia);
                try {
                    dataMediaPairService.createAndReturnId(dataMediaPair);
                    loadRouteService.createRoute(dataMediaPair.getPipelineId(), isRouteData, isLoadWideTableES, targetDataMedia, targetEsDataMedia, targetMqRabbitDataMedia);
                } catch (RepeatConfigureException rce) {
                    rce.printStackTrace();

                }
            }
        } catch (Exception e) {
            err.setMessage("invalidBatchDataMediaPair");
            LogUtils.log(ERROR, logger, () -> "=doBatchAdd=>error:%s", e);
            return;
        }
        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + pipelineId);
    }

    public void doEdit(@Param("submitKey") String submitKey, @Param("channelId") Long channelId,
                       @FormGroup("dataMediaPairInfo") Group dataMediaPairInfo,
                       @FormField(name = "formDataMediaPairError", group = "dataMediaPairInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        DataMediaPair dataMediaPair = new DataMediaPair();
        DataMedia sourceDataMedia = new DataMedia();
        DataMedia targetDataMedia = new DataMedia();
        DataMedia targetEsDataMedia = new DataMedia();
        DataMedia targetMqRabbitDataMedia = new DataMedia();
        dataMediaPairInfo.setProperties(dataMediaPair);

        int isRouteData = dataMediaPairInfo.getField("isRouteData").getIntegerValue();
        int isLoadWideTableES = dataMediaPairInfo.getField("isLoadWideTableES").getIntegerValue();

        targetEsDataMedia.setId(dataMediaPairInfo.getField("targetEsDataMediaId").getLongValue());
        targetMqRabbitDataMedia.setId(dataMediaPairInfo.getField("targetMqRabbitDataMediaId").getLongValue());


        // filter解析
        ExtensionDataType filterType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("filterType").getStringValue());
        ExtensionData filterData = new ExtensionData();
        filterData.setExtensionDataType(filterType);
        if (filterType.isClazz()) {
            filterData.setClazzPath(dataMediaPairInfo.getField("filterText").getStringValue());
        } else if (filterType.isSource()) {
            filterData.setSourceText(dataMediaPairInfo.getField("filterText").getStringValue());
        }
        dataMediaPair.setFilterData(filterData);

        // fileresovler解析
        ExtensionDataType resolverType = ExtensionDataType.valueOf(dataMediaPairInfo.getField("resolverType").getStringValue());
        ExtensionData resolverData = new ExtensionData();
        resolverData.setExtensionDataType(resolverType);
        if (resolverType.isClazz()) {
            resolverData.setClazzPath(dataMediaPairInfo.getField("resolverText").getStringValue());
        } else if (resolverType.isSource()) {
            resolverData.setSourceText(dataMediaPairInfo.getField("resolverText").getStringValue());
        }
        dataMediaPair.setResolverData(resolverData);

        sourceDataMedia.setId(dataMediaPairInfo.getField("sourceDataMediaId").getLongValue());
        dataMediaPair.setSource(sourceDataMedia);
        targetDataMedia.setId(dataMediaPairInfo.getField("targetDataMediaId").getLongValue());
        dataMediaPair.setTarget(targetDataMedia);
        try {
            dataMediaPairService.modify(dataMediaPair);
            // 修改 load route
            loadRouteService.editRoute(dataMediaPair.getPipelineId(), dataMediaPair.getTarget().getId(),
                    isRouteData, isLoadWideTableES, targetDataMedia, targetEsDataMedia, targetMqRabbitDataMedia);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaPair");
            return;
        }

        if (submitKey.equals("保存")) {
            nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + dataMediaPair.getPipelineId());
        } else if (submitKey.equals("下一步")) {
            nav.redirectToLocation("addColumnPair.htm?pipelineId=" + dataMediaPair.getPipelineId() + "&channelId="
                    + channelId + "&dataMediaPairId=" + dataMediaPair.getId() + "&sourceMediaId="
                    + sourceDataMedia.getId() + "&targetMediaId=" + targetDataMedia.getId());
        }
    }

    /**
     * 删除映射关系
     */
    public void doDelete(@Param("dataMediaPairId") Long dataMediaPairId, @Param("pipelineId") Long pipelineId,
                         Navigator nav) throws WebxException {
        Channel channel = channelService.findByPipelineId(pipelineId);
        if (channel.getStatus().isStart()) {
            nav.redirectTo(WebConstant.ERROR_FORBIDDEN_Link);
            return;
        }
        DataMediaPair dmp = dataMediaPairService.findById(dataMediaPairId);
        LoadRouteDO loadRouteDO = new LoadRouteDO();
        loadRouteDO.setPipelineId(dmp.getPipelineId());
        loadRouteDO.setTableId(dmp.getTarget().getId());
        loadRouteService.deleteByPipelineIdAndTableId(loadRouteDO);
        dataMediaPairService.remove(dataMediaPairId);
        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + pipelineId);
    }

    /**
     * 选择视图同步
     *
     * @throws Exception
     */
    public void doNextToView(@FormGroup("dataMediaPairInfo") Group dataMediaPairInfo,
                             @FormField(name = "formDataMediaPairError", group = "dataMediaPairInfo") CustomErrors err,
                             Navigator nav) throws Exception {
        DataMediaPair dataMediaPair = new DataMediaPair();
        DataMedia sourceDataMedia = new DataMedia();
        DataMedia targetDataMedia = new DataMedia();
        dataMediaPairInfo.setProperties(dataMediaPair);
        sourceDataMedia.setId(dataMediaPairInfo.getField("sourceDataMediaId").getLongValue());
        dataMediaPair.setSource(sourceDataMedia);
        targetDataMedia.setId(dataMediaPairInfo.getField("targetDataMediaId").getLongValue());
        dataMediaPair.setTarget(targetDataMedia);
        try {
            dataMediaPairService.create(dataMediaPair);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaPair");
            return;
        }

        nav.redirectToLocation("dataMediaPairList.htm?pipelineId=" + dataMediaPair.getPipelineId());
    }
}
