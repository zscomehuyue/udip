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
import com.alibaba.otter.manager.web.common.WebConstant;
import com.hwl.otter.clazz.tablerel.CheckTableRelService;
import com.hwl.otter.clazz.tablerel.dal.dataobject.CheckTableRelDo;

import javax.annotation.Resource;

public class CheckTableRelAction extends AbstractAction {

    @Resource(name = "checkTableRelService")
    public CheckTableRelService checkTableRelService;

    /**
     * 添加
     * 
     * @throws Exception
     */
    public void doAdd(@FormGroup("checkTableRel") Group checkTableRel,
                      @FormField(name = "formCheckTableRelError", group = "checkTableRel") CustomErrors err, Navigator nav)
                                                                                                                       throws Exception {

        CheckTableRelDo checkTableRelDo = new CheckTableRelDo();
        checkTableRel.setProperties(checkTableRelDo);


        try {
            checkTableRelService.insert(checkTableRelDo);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidData");
            return;
        }

        nav.redirectTo(WebConstant.Check_Table_Rel_LINK);
    }

    /**
     * @throws WebxException
     */
    public void doDelete(@Param("id") int id, @Param("pageIndex") int pageIndex,
                         @Param("searchKey") String searchKey, Navigator nav) throws WebxException {
        checkTableRelService.deleteCheckTableRel(id);

        nav.redirectToLocation("checkTableRelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }



    public void doEdit(@FormGroup("checkTableRel") Group checkTableRel, @Param("pageIndex") int pageIndex,
                       @Param("searchKey") String searchKey,
                       @FormField(name = "formCheckTableRelError", group = "checkTableRel") CustomErrors err, Navigator nav)
                                                                                                                        throws Exception {
        CheckTableRelDo checkTableRelDo = new CheckTableRelDo();
        checkTableRel.setProperties(checkTableRelDo);



        try {
            checkTableRelService.update(checkTableRelDo);
        } catch (RepeatConfigureException rce) {
            err.setMessage("invalidDataMedia");
            return;
        }
        nav.redirectToLocation("checkTableRelList.htm?pageIndex=" + pageIndex + "&searchKey=" + urlEncode(searchKey));
    }



}
