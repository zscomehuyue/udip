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

import com.alibaba.citrus.turbine.Navigator;
import com.alibaba.citrus.turbine.dataresolver.Param;
import com.alibaba.citrus.webx.WebxException;
import com.hwl.otter.clazz.repairlog.CheckRepairLogService;

import javax.annotation.Resource;

public class CheckRepairLogAction extends AbstractAction {

    @Resource(name = "checkRepairLogService")
    private CheckRepairLogService checkRepairLogService;




    /**
     * @throws WebxException
     */
    public void doDelete(@Param("id") Long id, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey,@Param("sourceSchema") String sourceSchema,
                         @Param("sourceTable") String sourceTable,@Param("isSuccess") String isSuccess, Navigator nav) throws WebxException {
        checkRepairLogService.deleteById(id);

        nav.redirectToLocation("checkRepairLogList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey)
                + "&sourceSchema="+urlEncode(sourceSchema) + "&sourceTable="+urlEncode(sourceTable)+"&isSuccess="+isSuccess);
    }






}
