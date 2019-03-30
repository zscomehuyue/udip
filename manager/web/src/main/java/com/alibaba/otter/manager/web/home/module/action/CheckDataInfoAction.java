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
import com.alibaba.citrus.service.form.Field;
import com.alibaba.citrus.service.form.Group;
import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.FormField;
import com.alibaba.citrus.turbine.dataresolver.FormGroup;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.alibaba.otter.manager.biz.common.exceptions.RepeatConfigureException;
import com.alibaba.otter.manager.biz.utils.DateUtils;
import com.hwl.otter.clazz.datacheck.DataCheckService;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;

import javax.annotation.Resource;

public class CheckDataInfoAction extends AbstractAction {

    @Resource(name = "dataCheckService")
    private DataCheckService dataCheckService;




    /**
     * @throws WebxException
     */
    public void doDelete(@Param("id") int id, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey,@Param("sourceSchema") String sourceSchema,
                         @Param("targetSchema") String targetSchema, Navigator nav) throws WebxException {
        dataCheckService.deleteCheckData(id);

        nav.redirectToLocation("checkDataLogList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey)
                + "&sourceSchema="+urlEncode(sourceSchema) + "&targetSchema="+urlEncode(targetSchema));
    }



    public void doEdit(@FormGroup("checkDataInfo") Group checkDataInfo, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,@Param("sourceSchema") String sourceSchema,@Param("targetSchema") String targetSchema,
                       @FormField(name = "formCheckDataInfoError", group = "checkDataInfo") CustomErrors err, Navigator nav)
                                                                                                                        throws Exception {



        try {
            DataCheckDo dataCheckDo = new DataCheckDo();
            checkDataInfo.setProperties(dataCheckDo);
            dataCheckDo = dataCheckService.getCheckDataLogById(dataCheckDo.getId());

            Field checkBeginDate = checkDataInfo.getField("checkBeginDate");
            dataCheckDo.setCheckBeginDate(DateUtils.getTimestamp(checkBeginDate.getStringValue(),"yyyy-MM-dd HH:mm:ss"));

            Field checkEndDate = checkDataInfo.getField("checkEndDate");
            dataCheckDo.setCheckEndDate(DateUtils.getTimestamp(checkEndDate.getStringValue(),"yyyy-MM-dd HH:mm:ss"));


            dataCheckService.updateDataCheckDoById(dataCheckDo);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMedia");
            return;
        }
        nav.redirectToLocation("checkDataLogList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey)
                + "&sourceSchema="+urlEncode(sourceSchema) + "&targetSchema="+urlEncode(targetSchema));
    }



}
