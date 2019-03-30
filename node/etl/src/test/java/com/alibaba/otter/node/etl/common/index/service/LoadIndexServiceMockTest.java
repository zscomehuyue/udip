package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.common.push.index.wide.config.CurriculumWideIndexConstants;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.shared.etl.model.EventData;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.CollectionUtils;
import org.testng.collections.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;

public class LoadIndexServiceMockTest extends BaseDataServiceMock {

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    protected ConfigClientService configClientService;

    @Mock
    private LoadIndexService loadIndexService;

    private List<WideTable> getMasterByTableId(List<WideTable> table, Long tableId) {
        return table.stream().filter(wideTable -> wideTable.getMainTable().getId().equals(tableId)).collect(Collectors.toList());
    }

    @Test
    public void test() {
        long tableId = 26L;
        List<WideTable> allWideTables = getAllWideTables();
        Mockito.doReturn(allWideTables).when(configClientService).findWideTable(Mockito.anyLong(), Mockito.eq(tableId));
        List<WideTable> tableIdWideList = configClientService.findWideTable(esIndex.getId(), tableId);
        List<WideTable> masterTables = getMasterByTableId(tableIdWideList, tableId);
        boolean isMaster = !CollectionUtils.isEmpty(masterTables);
        List<WideTable> allIndexWideTables = Lists.newArrayList();
        if (!isMaster) {
            tableIdWideList.forEach(wideTable -> {
                        allIndexWideTables.addAll(configClientService.findWideTable(esIndex.getId(), wideTable.getMainTable().getId()));
                    }
            );
        } else {
            allIndexWideTables.addAll(tableIdWideList);
        }

        Map<DataMedia, List<WideTable>> wideIndexMap = allIndexWideTables.stream().distinct().collect(Collectors.toMap(WideTable::getTarget, wideTable ->
                Lists.newArrayList(wideTable), (List<WideTable> newList, List<WideTable> oldList) -> {
            oldList.addAll(newList);
            return oldList;
        }));
        wideIndexMap.entrySet().forEach(entry -> {
            System.out.println("index = " + entry.getKey().getNameMode());
            entry.getValue().forEach(wideTable -> {
                System.out.println(wideTable.getSlaveTable().getName());
            });
        });
    }

    @Test
    public void insertMaster() {
        long tableId = 26L;
        Pipeline pipeline = getPipeline();
        OperateType update = OperateType.INSERT;
        List<WideTable> allWideTables = getAllWideTables();

        int change_in = 2;
        int change_out = 3;
        List<EventData> addDatas = getAddDatas(change_out, change_in);
        final int maxPerson = 20;
        final int registCount = 2;
        final String clazz_levelId_01 = "clazz_levelId_01";
        final String clazz_servicecenterId_01 = "clazz_servicecenterId_01";

        operateWideIdMap.entrySet().forEach(entry -> {
            entry.getValue().put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put("clazz_levelId", clazz_levelId_01);
            entry.getValue().put("clazz_servicecenterId", clazz_servicecenterId_01);

            entry.getValue().put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, clazz_servicecenterId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, classTimeId.getColumnValue());

            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME, cucClassId.getColumnValue());
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + change_out - change_in - registCount);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, classtimeTypeId.getColumnValue());

        });

        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);
//        doReturn(new int[]{1}).when(indexService).batchSaveByIds(eq(index_name), eq(index_type), refEq(operateWideIdMap,"curriculum_changeinCourseNum","curriculum_changeoutCourseNum"));//FIXME
        System.out.println("index=" + index_name + ",type=" + index_type + JsonUtils.marshalToString(operateWideIdMap).replaceAll(",", "\n"));

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(cucClassId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
                    put("clazz_levelId", clazz_levelId_01);
                    put("clazz_servicecenterId", clazz_servicecenterId_01);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("clazz"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(classtimeTypeId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, classtimeTypeId.getColumnValue());
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classtime_type"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(cucClassId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME, cucClassId.getColumnValue());
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("class_regist_count"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(classTimeId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, classTimeId.getColumnValue());
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classtime"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(clazz_levelId_01, new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classlevel"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(clazz_servicecenterId_01, new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, clazz_servicecenterId_01);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("department"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        loadIndexService = Mockito.spy(LoadIndexService.class);
        loadIndexService.setConfigClientService(configClientService);
        loadIndexService.setEventDataIndexService(eventDataIndexService);
        loadIndexService.setIndexConfigServiceFactory(indexConfigServiceFactory);

        Mockito.doReturn(allWideTables).when(configClientService).findWideTable(Mockito.anyLong(), Mockito.eq(tableId));

        int[] ints = loadIndexService.loadWideIndex(esIndex, addDatas, update, tableId, pipeline);
        Mockito.verify(loadIndexService, Mockito.times(1)).loadWideIndex(esIndex, addDatas, update, tableId, pipeline);
        Mockito.verify(eventDataIndexService, Mockito.times(1)).batchWideSaveByIds(esIndex.getNamespace(), esIndex.getName(), addDatas, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);


    }

}
