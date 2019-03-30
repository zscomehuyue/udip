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
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.es.IndexMediaSource;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;

import javax.annotation.Resource;

public class MqRabbitDataMediaSourceAction extends AbstractAction {

    @Resource(name = "dataMediaSourceService")
    private DataMediaSourceService dataMediaSourceService;

    @Resource(name = "dataMediaService")
    private DataMediaService       dataMediaService;

    /**
     * 添加Channel
     * 
     * @param channelInfo
     * @param channelParameterInfo
     * @throws Exception
     */
    public void doAdd(@FormGroup("mqRabbitDataMediaSourceInfo") Group dataMediaSourceInfo,
                      @FormField(name = "formMqDataMediaSourceError", group = "mqRabbitDataMediaSourceInfo") CustomErrors err,
                      Navigator nav) throws Exception {
        DataMediaSource dataMediaSource = new DataMediaSource();
        dataMediaSourceInfo.setProperties(dataMediaSource);

        if (dataMediaSource.getType().isRabbit()) {
            MqMediaSource mqMediaSource = new MqMediaSource();
            dataMediaSourceInfo.setProperties(mqMediaSource);
            try {
                dataMediaSourceService.create(mqMediaSource);
            } catch (RepeatConfigureException rce) {
                err.setMessage("invalidDataMediaSource");
                return;
            }
        }

        nav.redirectTo(WebConstant.MQ_RABBIT_DATA_MEDIA_SOURCE_LIST_LINK);
    }

    /**
     * @param channelId
     * @throws WebxException
     */
    public void doDelete(@Param("dataMediaSourceId") Long dataMediaSourceId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        if (dataMediaService.listByDataMediaSourceId(dataMediaSourceId).size() < 1) {
            dataMediaSourceService.remove(dataMediaSourceId);
        }

        nav.redirectToLocation("mqRabbitDataSourceList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    public void doEdit(@FormGroup("mqRabbitDataMediaSourceInfo") Group dataMediaSourceInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,
                       @FormField(name = "formMqDataMediaSourceError", group = "mqRabbitDataMediaSourceInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        MqMediaSource mqMediaSource = new MqMediaSource();
        dataMediaSourceInfo.setProperties(mqMediaSource);


        try {
            dataMediaSourceService.modify(mqMediaSource);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMediaSource");
            return;
        }

        nav.redirectToLocation("mqRabbitDataSourceList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

}
