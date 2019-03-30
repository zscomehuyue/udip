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
import com.alibaba.otter.manager.biz.config.widetable.WideTableService;
import com.alibaba.otter.manager.biz.config.widetable.dal.dataobject.WideTableDO;
import com.alibaba.otter.manager.web.common.WebConstant;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WideTableAction extends AbstractAction {

    @Resource(name = "wideTableService")
    private WideTableService wideTableService;


    /**
     * 添加宽表
     * 
     * @throws Exception
     */
    public void doAdd(@FormGroup("wideTableInfo") Group wideTableInfo,
                      @FormField(name = "formWideTableInfoError", group = "wideTableInfo") CustomErrors err,
                      Navigator nav) throws Exception {
        WideTableDO wideTableDO = new WideTableDO();
        wideTableInfo.setProperties(wideTableDO);

        if(wideTableDO.getMainTableId() == wideTableDO.getSlaveTableId()){
            err.setMessage("invalidMainSlave");
            return;
        }

        Map<String,Long> mp = new HashMap<>();
        mp.put("targetId",wideTableDO.getTargetId());
        mp.put("mainTableId",wideTableDO.getMainTableId());
        mp.put("slaveTableId",wideTableDO.getSlaveTableId());
        List<WideTable> ls= wideTableService.listByCondition(mp);
        if(!CollectionUtils.isEmpty(ls)){
            err.setMessage("invalidRepetitionID");
            return;
        }

        wideTableService.insert(wideTableDO);


        nav.redirectTo(WebConstant.WIDE_TABLE_LIST_LINK);
    }

    /**
     * @throws WebxException
     */
    public void doDelete(@Param("wideTableId") Long wideTableId, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        wideTableService.remove(wideTableId);

        nav.redirectToLocation("wideTableList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

    public void doEdit(@FormGroup("wideTableInfo") Group wideTableInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,
                       @FormField(name = "formWideTableInfoError", group = "wideTableInfo") CustomErrors err,
                       Navigator nav) throws Exception {
        WideTableDO wideTableDO = new WideTableDO();
        wideTableInfo.setProperties(wideTableDO);


        if(wideTableDO.getMainTableId() == wideTableDO.getSlaveTableId()){
            err.setMessage("invalidMainSlave");
            return;
        }

        wideTableService.update(wideTableDO);

        nav.redirectToLocation("wideTableList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }

}
