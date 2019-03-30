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
import com.hwl.otter.clazz.datacheck.DataCheckService;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;

import javax.annotation.Resource;

/**
 * 类AddDataMedia.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2011-10-25 上午10:00:32
 */
public class EditCheckDataLog {

    @Resource(name = "dataCheckService")
    private DataCheckService dataCheckService;

    public void execute(@Param("id") Long id, @Param("pageIndex") int pageIndex,
                        @Param("sourceSchema") String sourceSchema,@Param("targetSchema") String targetSchema,
                        @Param("searchKey") String searchKey, Context context) throws Exception {
        DataCheckDo dataCheckDo = dataCheckService.getCheckDataLogById(id);
        context.put("dataCheckDo", dataCheckDo);
        context.put("sourceSchema", sourceSchema);
        context.put("targetSchema", targetSchema);
        context.put("pageIndex", pageIndex);
        context.put("searchKey", searchKey);
    }

}
