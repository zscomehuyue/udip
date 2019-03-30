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

package com.alibaba.otter.manager.web.home.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.*;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

public class DataMediaPairInfo {

    @Resource(name = "channelService")
    private ChannelService       channelService;

    @Resource(name = "dataMediaPairService")
    private DataMediaPairService dataMediaPairService;


    public void execute(@Param("dataMediaPairId") Long dataMediaPairId, Context context) throws Exception {
        DataMediaPair dataMediaPair = dataMediaPairService.findById(dataMediaPairId);
        Channel channel = channelService.findByPipelineId(dataMediaPair.getPipelineId());

        List<ColumnPair> columnPairs = dataMediaPair.getColumnPairs();
        List<ColumnGroup> columnGroups = dataMediaPair.getColumnGroups();
        // 暂时策略，只拿出list的第一个Group
        ColumnGroup columnGroup = new ColumnGroup();
        if (!CollectionUtils.isEmpty(columnGroups)) {
            columnGroup = columnGroups.get(0);
        }

        List<LoadRoute> loadRouteList = dataMediaPair.getLoadRouteList();
        int isRouteData = 1;
        int isLoadWideTableES = 1;
        for(LoadRoute route : loadRouteList){
            DataMedia temp = route.getLoadDataMedia();
            if(temp == null){
                continue;
            }
            if(temp.getSource().getType().isMysql()){
                isRouteData = 0;
            }else if(temp.getSource().getType().isEs()){
                isLoadWideTableES = route.getType().getType();
            }
        }

        context.put("dataMediaPair", dataMediaPair);
        context.put("isRouteData", isRouteData);
        context.put("isLoadWideTableES", isLoadWideTableES);
        context.put("columnGroup", columnGroup);
        context.put("columnPairs", columnPairs);
        context.put("channelId", channel.getId());
    }
}
