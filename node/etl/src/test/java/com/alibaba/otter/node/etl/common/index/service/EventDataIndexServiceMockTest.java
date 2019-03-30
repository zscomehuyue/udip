package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.config.CurriculumWideIndexConstants;
import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.common.push.index.wide.config.IndexConfigServiceFactory;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.shared.etl.model.EventData;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class EventDataIndexServiceMockTest extends BaseDataServiceMock {

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSimpleBatchDeleteByIds() {
        List<EventData> data = prepareData();

        doReturn(new int[]{1}).when(indexService).batchDeleteByIds(index_name, index_type, ids, null);
        int[] ints = eventDataIndexService.batchDeleteByIds(index_name, index_type, data);
        verify(eventDataIndexService, times(1)).batchDeleteByIds(index_name, index_type, data);

        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }


    @Test
    public void testBatchSingleSaveByIds() {
        List<EventData> data = prepareData();
        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);
        int[] ints = eventDataIndexService.batchSingleSaveByIds(index_name, index_type, data);
        verify(eventDataIndexService, times(1)).batchSingleSaveByIds(index_name, index_type, data);

        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }


    @Test
    public void testBatchSingleUpdateByIds() {
        List<EventData> data = prepareData();

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        int[] ints = eventDataIndexService.batchSingleUpdateByIds(index_name, index_type, data);
        verify(eventDataIndexService, times(1)).batchSingleUpdateByIds(index_name, index_type, data);

        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }


    @Test
    public void testWideSaveWithDefaultValue() {
        List<EventData> dataList = getAddDatas(0, 0);
        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);
        doReturn(Maps.newHashMap()).when(indexService).getDataMapByIds(Mockito.anyString(), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testWideAddWithStatus() {
        List<EventData> dataList = getAddDatas(0, 0);
        final int maxPerson = 20;
        final int registCount = 10;
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

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson - registCount);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, classtimeTypeId.getColumnValue());
        });

        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);
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


        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);

        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testWideAddWithChangeField() {
        int change_in = 2;
        int change_out = 3;
        List<EventData> dataList = getAddDatas(change_out, change_in);
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

//        //System.out.println("index="+index_name+",type="+index_type+JsonUtils.marshalToString(operateWideIdMap).replaceAll(",","\n"));

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

        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }


    @Test
    public void testWideAddAndIgnoreOldMasterValues() {
        int change_in = 1;
        int change_out = 3;
        List<EventData> dataList = getAddDatas(change_out, change_in);
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


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(indexConfigServiceFactory.getDefaultFormatField(index_name, cucId.getColumnName()), cucId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField(index_name, cucClassId.getColumnName()), cucClassId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField(index_name, classtimeTypeId.getColumnName()), classtimeTypeId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField(index_name, classTimeId.getColumnName()), classTimeId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField(index_name, changeIn.getColumnName()), 11L);
                    put(indexConfigServiceFactory.getDefaultFormatField(index_name, changeOut.getColumnName()), 11L);

                    put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                    put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));

                    put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);
                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


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

        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testWideAddAndNoRegistCount() {
        int change_in = 1;
        int change_out = 3;
        List<EventData> dataList = getAddDatas(change_out, change_in);
        final int maxPerson = 20;
        final int registCount = 0;
        final String clazz_levelId_01 = "clazz_levelId_01";
        final String clazz_servicecenterId_01 = "clazz_servicecenterId_01";

        operateWideIdMap.entrySet().forEach(entry -> {

            //FIXME CHECKIT the status is 1;
            entry.getValue().put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put("clazz_levelId", clazz_levelId_01);
            entry.getValue().put("clazz_servicecenterId", clazz_servicecenterId_01);

            entry.getValue().put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, clazz_servicecenterId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, classTimeId.getColumnValue());

            //FIXME no regist count values ;
//            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME, cucClassId.getColumnValue());
//            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + change_out - change_in - registCount);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, classtimeTypeId.getColumnValue());

        });

        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);


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
                //FIXME NO regist count value ;
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

        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testWideAddNotGetClasstimeId() {
        int change_in = 1;
        int change_out = 3;
        List<EventData> dataList = getAddDatas(change_out, change_in);
        final int maxPerson = 20;
        final int registCount = 0;
        final String clazz_levelId_01 = "clazz_levelId_01";
        final String clazz_servicecenterId_01 = "clazz_servicecenterId_01";

        operateWideIdMap.entrySet().forEach(entry -> {

            //FIXME CHECKIT the status is 1;
            entry.getValue().put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put("clazz_levelId", clazz_levelId_01);
            entry.getValue().put("clazz_servicecenterId", clazz_servicecenterId_01);

            entry.getValue().put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, clazz_servicecenterId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);

            //FIXME not the field
//            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, classTimeId.getColumnValue());

            //FIXME no regist count values ;
//            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME, cucClassId.getColumnValue());
//            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + change_out - change_in - registCount);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, classtimeTypeId.getColumnValue());

        });

        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);


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
                //FIXME NO regist count value ;
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("class_regist_count"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //FIXME not get value by fk id ;
//                put(classTimeId.getColumnValue(), new HashMap<String, Object>() {{
//                    put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, classTimeId.getColumnValue());
//                }});
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

        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    /**
     * 当没有获取不到classId时，状态为默认值，计算值为默认值；
     */
    @Test
    public void testWideAddNotGetClassId() {
        int change_in = 1;
        int change_out = 3;
        List<EventData> dataList = getAddDatas(change_out, change_in);
        final int maxPerson = 20;
        final int registCount = 0;
        final String clazz_levelId_01 = "clazz_levelId_01";
        final String clazz_servicecenterId_01 = "clazz_servicecenterId_01";

        operateWideIdMap.entrySet().forEach(entry -> {

            //FIXME CHECKIT the status is 0;
            entry.getValue().put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);

            //FIXME no class id
//            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
//            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
//            entry.getValue().put("clazz_levelId", clazz_levelId_01);
//            entry.getValue().put("clazz_servicecenterId", clazz_servicecenterId_01);

            //FIXME no field when no classId
//            entry.getValue().put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, clazz_servicecenterId_01);
//            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);

            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, classTimeId.getColumnValue());

            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME, cucClassId.getColumnValue());
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            //FIXME no class id   the remainCount preRemainCount is default value
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);

            entry.getValue().put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, classtimeTypeId.getColumnValue());

        });

        doReturn(new int[]{1}).when(indexService).batchSaveByIds(index_name, index_type, operateWideIdMap, null);

//        //System.out.println("index="+index_name+",type="+index_type+ JsonUtils.marshalToString(operateWideIdMap).replaceAll(",","\n"));

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
//                FIXME no class id
//                put(cucClassId.getColumnValue(), new HashMap<String, Object>() {{
//                    put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
//                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
//                    put("clazz_levelId", clazz_levelId_01);
//                    put("clazz_servicecenterId", clazz_servicecenterId_01);
//                }});
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
                //FIXME not get it when no classId  ;
//                put(clazz_levelId_01, new HashMap<String, Object>() {{
//                    put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, clazz_levelId_01);
//                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classlevel"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //FIXME not get it  when no classId  ;
//                put(clazz_servicecenterId_01, new HashMap<String, Object>() {{
//                    put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, clazz_servicecenterId_01);
//                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("department"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        List<WideTable> allWideTables = getAllWideTables();
        Pipeline pipeline = getPipeline();
        int[] ints = eventDataIndexService.batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideSaveByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testSplitIndex() {

    }

    @Test
    public void testNoSplitIndexKey() {

    }

    @Test
    public void testAddRegistCountTable() {
        final int registCount = 10;
        final int maxPerson = 20;
        final int changeOutNum = 2;
        final int changeInNum = 2;

        List<EventData> dataList = getRegistCount(registCount);
        List<WideTable> allWideTables = getAllWideTables();

        //Query Es and Return Values ;
        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, changeOutNum);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, changeInNum);

                    put("curriculum_classId", cucClassId.getColumnValue());

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + changeOutNum - changeInNum - 0);
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - 0);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());
        Pipeline pipeline = getPipeline();

        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, changeOutNum);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, changeInNum);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + changeOutNum - changeInNum - registCount);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - registCount);

            entry.getValue().put("curriculum_classId", cucClassId.getColumnValue());
        });

//
        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
                System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv).getClass().getSimpleName());
            });
        });


        doReturn(new int[]{1}).when(indexService).batchUpdateByIds("curriculum", index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(registCountTabel.getTarget().getNamespace(), index_type, dataList, allWideTables, registCountTabel, pipeline, OperateType.INSERT);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(registCountTabel.getTarget().getNamespace(), index_type, dataList, allWideTables, registCountTabel, pipeline, OperateType.INSERT);

        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);

    }

    @Test
    public void testAddRegistCountTableAndNotGetValues() {
        final int registCount = 10;
        List<EventData> dataList = getRegistCount(registCount);
        List<WideTable> allWideTables = getAllWideTables();

        //Query Es and Return Values ;
        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //NOT get values for regist count add ;
            }
        }).when(indexService).getDataMapByIds(Mockito.anyString(), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());
        Pipeline pipeline = getPipeline();


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {
        });

        doReturn(new int[0]).when(indexService).batchUpdateByIds("curriculum", index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(registCountTabel.getTarget().getNamespace(), index_type, dataList, allWideTables, registCountTabel, pipeline, OperateType.INSERT);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(registCountTabel.getTarget().getNamespace(), index_type, dataList, allWideTables, registCountTabel, pipeline, OperateType.INSERT);

        Assert.assertTrue(ints.length == 0);

    }

    @Test
    public void testUpdateMasterChangeInValue() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 1;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);

                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, 2l);

                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + change_out - 2 - 0);
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - 0);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.anyString(), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);

            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            Long remainCount = maxPerson + change_out - change_in - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, remainCount.intValue());
            Long preCount = maxPerson - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, preCount.intValue());

        });

//        operateWideIdMap.keySet().forEach(key -> {
//            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv).getClass().getSimpleName());
//            });
//        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap);

//
        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);

    }

    @Test
    public void testUpdateMasterChangeOutValue() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 0;
        long change_out = 2;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);

                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, 2l);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);

                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + change_out - 2 - 0);
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - 0);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.anyString(), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);

            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            Long remainCount = maxPerson + change_out - change_in - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, remainCount.intValue());
            Long preCount = maxPerson - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, preCount.intValue());

        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap);

//
        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateMasterChangeOutAndInValue() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 20;
        long change_out = 2;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);

                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, 2l);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, 100l);

                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, maxPerson + change_out - 2 - 0);
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, maxPerson - 0);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.anyString(), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);

            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            Long remainCount = maxPerson + change_out - change_in - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, remainCount.intValue());
            Long preCount = maxPerson - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, preCount.intValue());

        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap);

//
        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateMasterIgnoreField() {
        long change_in = 0;
        long change_out = -100;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        Pipeline pipeline = getPipeline();

        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 0);

    }

    /**
     * FIXME slavePkidName               RealTableFkIdName
     * 1    curriculum	26	curriculum_id	17	clazz_id	                curriculum_classId	        26	25		2018-07-11 14:32:00	2018-07-11 20:53:32
     * 2	curriculum	26	curriculum_id	24	classtimeType_id	        curriculum_classtimeTypeId	26	25		2018-07-11 14:35:37	2018-07-11 20:53:07
     * 4	curriculum	26	curriculum_id	22	classRegistCount_classId	curriculum_classId	        26	25		2018-07-11 14:40:06	2018-07-11 22:08:48
     * 6	curriculum	26	curriculum_id	18	classtime_id	            curriculum_classtimeId	    26	25		2018-08-03 16:27:14	2018-08-03 16:29:29
     * <p>
     * 3    curriculum	26	curriculum_id	23	classlevel_id	            clazz_levelId	            26	25		2018-07-11 14:37:55	2018-07-11 20:52:16
     * 5	curriculum	26	curriculum_id	21	department_id	            clazz_servicecenterId	    26	25		2018-07-11 14:43:22	2018-07-11 20:48:45
     * <p>
     * <p>
     * 7	regist_stage 52	rgse_id	       51	stu_id	                    rgse_studentId	            0	46	    2018-12-19 18:00:08	2018-12-20 14:26:28		stu_id
     * 8	regist_stage 52	rgse_id	       50	rg_id	                    rgse_registId	            0	46		2018-12-20 14:28:23	2018-12-20 14:28:23		rg_id
     * 9	regist_stage 52	rgse_id	       2	clazz_id	                rgse_classId	            0	46		2018-12-20 14:33:32	2018-12-20 14:33:32		clazz_id
     * 10	regist_stage 52	rgse_id	       57	clase_id	                rgse_classStageId	        0	46		2018-12-20 14:54:12	2018-12-20 14:54:12		clase_id
     * <p>
     * <p>
     */
    @Test
    public void testUpdateMasterUpdateFkIdOfClassId() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        cucClassId.setUpdate(true);


        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(cucId.getColumnName(), cucId.getColumnValue());
                    put("clazz_id", "over_id");
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        HashMap<String, Map<String, Object>> clazz = new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucClassId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
                    put("clazz_year", "2018");
                    put("clazz_id", cucClassId.getColumnValue());
                    put("classlevel_id", "classlevel_id_01");
                    put("department_id", "department_id_01");
                }});
            }
        };
        getPkid().forEach(pkid -> clazz.get(cucClassId.getColumnValue()).putIfAbsent(pkid, "1"));


        doReturn(clazz).when(indexService).getDataMapByIds(Mockito.eq("clazz"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        final String class_regist_count_id_01 = "classRegistCount_id_01";
        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :FIXME 关联键是 classRegistCount_classId
                put(cucClassId.getColumnValue(), new HashMap<String, Object>() {{
                    put("clazz_id", cucClassId.getColumnValue());
                    put("classRegistCount_id", class_regist_count_id_01);
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("class_regist_count"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put("classlevel_id_01", new HashMap<String, Object>() {{
                    put("classlevel_id", "classlevel_id_01");
                    put("clazz_id", cucClassId.getColumnValue());
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classlevel"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put("department_id_01", new HashMap<String, Object>() {{
                    put("department_id", "department_id_01");
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("department"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put("clazz_id", cucClassId.getColumnValue());
            entry.getValue().put("clazz_year", "2018");
            entry.getValue().put("classRegistCount_id", class_regist_count_id_01);
            entry.getValue().put("classlevel_id", "classlevel_id_01");
            entry.getValue().put("department_id", "department_id_01");
            entry.getValue().put(cucId.getColumnName(), cucId.getColumnValue());
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            Long remainCount = maxPerson + change_out - change_in - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, remainCount.intValue());
            Long preCount = maxPerson - registCount;
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, preCount.intValue());

            entry.getValue().put("esStatus", 1);
            getPkid().forEach(pkid -> {
                entry.getValue().putIfAbsent(pkid, "1");
            });
        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);

    }

    @Test
    public void testUpdateMasterUpdateFkIdOfClasstimeId() {
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        classTimeId.setUpdate(true);

        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(cucId.getColumnName(), cucId.getColumnValue());
                    put(classTimeId.getColumnName(), "over_id");
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(classTimeId.getColumnValue(), new HashMap<String, Object>() {{
                    put(classTimeId.getColumnName(), classTimeId.getColumnValue());

                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classtime"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

//            entry.getValue().put(cucId.getColumnName(), cucId.getColumnValue());
            entry.getValue().put(classTimeId.getColumnName(), classTimeId.getColumnValue());

        });
        operateWideIdMap.get(cucId.getColumnValue()).remove("remainCount");
        operateWideIdMap.get(cucId.getColumnValue()).remove("preRemainCount");

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);

    }

    @Test
    public void testUpdateMasterUpdateFkIdOfClasstimeTypeId() {
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getWideUpdate(change_out, change_in);
        classtimeTypeId.setUpdate(true);

        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    put(cucId.getColumnName(), cucId.getColumnValue());
                    put(classTimeId.getColumnName(), "over_id");
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(classtimeTypeId.getColumnValue(), new HashMap<String, Object>() {{
                    put("classtimeType_id", classtimeTypeId.getColumnValue());

                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classtime_type"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {
            entry.getValue().put("classtimeType_id", classtimeTypeId.getColumnValue());

        });
        operateWideIdMap.get(cucId.getColumnValue()).remove("remainCount");
        operateWideIdMap.get(cucId.getColumnValue()).remove("preRemainCount");

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        verify(eventDataIndexService, times(1)).batchWideUpdateByIds(index_name, index_type, dataList, allWideTables, pipeline);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);

    }

    @Test
    public void testUpdateClassMaxPerson() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getClassUpdate(maxPerson);
        maxPersonColumn.setUpdate(true);


        Pipeline pipeline = getPipeline();


        HashMap<String, Map<String, Object>> toBeReturned = new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    long maxPersonOld = maxPerson * 2;


                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
                    put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());
                    put("curriculum_classId", cucClassId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField("clazz", classlevelId.getColumnName()), classlevelId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField("clazz", classDepartmentId.getColumnName()), classDepartmentId.getColumnValue());

                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPersonOld);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPersonOld + change_out - change_in - registCount)).intValue());
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPersonOld - registCount)).intValue());
                }});
            }
        };
        getPkid().forEach(pkid -> {
            toBeReturned.get(cucId.getColumnValue()).putIfAbsent(pkid, "1");
        });
        doReturn(toBeReturned).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {


            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
            entry.getValue().put("curriculum_classId", cucClassId.getColumnValue());

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPerson + change_out - change_in - registCount)).intValue());
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPerson - registCount)).intValue());
            entry.getValue().put("esStatus", 1);
            getPkid().forEach(pkid -> {
                entry.getValue().putIfAbsent(pkid, "1");
            });
        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv).getClass().getName());
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateClassFkClassLevelId() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getClassUpdate(maxPerson);
        classlevelId.setUpdate(true);


        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    long maxPersonOld = maxPerson;


                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
                    put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());

                    put("curriculum_classId", cucClassId.getColumnValue());

                    put("classlevel_degree", "degree_over");
                    put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, "2222");

                    put(indexConfigServiceFactory.getDefaultFormatField("clazz", classlevelId.getColumnName()), classlevelId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField("clazz", classDepartmentId.getColumnName()), classDepartmentId.getColumnValue());

                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPersonOld);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPersonOld + change_out - change_in - registCount)).intValue());
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPersonOld - registCount)).intValue());
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(classlevelId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, classlevelId.getColumnValue());
                    put("classlevel_degree", "degree_01");

                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("classlevel"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, classlevelId.getColumnValue());

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
            entry.getValue().put("curriculum_classId", cucClassId.getColumnValue());
            entry.getValue().put("classlevel_degree", "degree_01");

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPerson + change_out - change_in - registCount)).intValue());
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPerson - registCount)).intValue());


        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv).getClass().getName());
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateClassFkDepartmentId() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getClassUpdate(maxPerson);
        classDepartmentId.setUpdate(true);


        Pipeline pipeline = getPipeline();


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    long maxPersonOld = maxPerson;


                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
                    put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, cucClassId.getColumnValue());

                    put("curriculum_classId", cucClassId.getColumnValue());

                    put("department_districtId", "district_id_0ver");
                    put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, "2222");

                    put(indexConfigServiceFactory.getDefaultFormatField("clazz", classlevelId.getColumnName()), classlevelId.getColumnValue());
                    put(indexConfigServiceFactory.getDefaultFormatField("clazz", classDepartmentId.getColumnName()), classDepartmentId.getColumnValue());

                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPersonOld);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPersonOld + change_out - change_in - registCount)).intValue());
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPersonOld - registCount)).intValue());
                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(classDepartmentId.getColumnValue(), new HashMap<String, Object>() {{
                    put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, classDepartmentId.getColumnValue());
                    put("department_districtId", "district_id_01");

                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("department"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
            entry.getValue().put("curriculum_classId", cucClassId.getColumnValue());


            entry.getValue().put("department_districtId", "district_id_01");
            entry.getValue().put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, classDepartmentId.getColumnValue());


            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPerson + change_out - change_in - registCount)).intValue());
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPerson - registCount)).intValue());


        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateClassNoCareField() {
        final long maxPerson = 20;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getClassUpdate(maxPerson);
        classDepartmentId.setUpdate(true);
        classDepartmentId.setColumnName("no_care_filed ");
        Pipeline pipeline = getPipeline();

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, clazzWideTable, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 0);
    }

    @Test
    public void testUpdateRegistCountValue() {
        final long registCount = 10;
        final long maxPerson = 20;
        long change_in = 0;
        long change_out = 0;
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getRegistCount(registCount);
        registCountColumn.setUpdate(true);

        Pipeline pipeline = getPipeline();


        HashMap<String, Map<String, Object>> curriculum_classId = new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{
                    long maxPersonOld = maxPerson;


                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());

                    put("curriculum_classId", cucClassId.getColumnValue());
                    long registCount = 1111L;
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

                    put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPersonOld);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
                    put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
                    put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);


//                    put(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME, "1");
//                    put(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME, "1");
//                    put(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME, "1");
//                    put(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME, "1");
//                    put(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME, "1");


                    put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPersonOld + change_out - change_in - registCount)).intValue());
                    put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPersonOld - registCount)).intValue());
                }});
            }
        };
        getPkid().forEach(pkid -> {
            curriculum_classId.get(cucId.getColumnValue()).putIfAbsent(pkid, "1");
        });
        doReturn(curriculum_classId).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());


        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
            entry.getValue().put("curriculum_classId", cucClassId.getColumnValue());

            entry.getValue().put(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS, maxPerson);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, change_out);
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, change_in);
            entry.getValue().put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, registCount);

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, ((Long) (maxPerson + change_out - change_in - registCount)).intValue());
            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, ((Long) (maxPerson - registCount)).intValue());
            entry.getValue().put(IndexConfigServiceFactory.ES_STATUS, 1);

            getPkid().forEach(pkid -> {
                entry.getValue().putIfAbsent(pkid, "1");
            });
        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
                System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);

        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, registCountTabel, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, registCountTabel, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    public List<String> getPkid() {
        return Lists.newArrayList(
                CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                , CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME
                , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME);
    }

    @Test
    public void testUpdateDeparent() {
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getDepartUpdate();
        districtId.setUpdate(true);

        Pipeline pipeline = getPipeline();

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{

                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
                    put("clazz_servicecenterId", classDepartmentId.getColumnValue());
                    put("department_districtId", "district_id_0ver");


                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());


            entry.getValue().put("clazz_servicecenterId", classDepartmentId.getColumnValue());


        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, deparetTable, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, deparetTable, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateClasstime() {
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getClasstimeUpdate();
        timeNameId.setUpdate(true);

        Pipeline pipeline = getPipeline();

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{

                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
                    put("curriculum_classtimeId", classtimeId.getColumnValue());
                    put("classtime_timeName", "over");


                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
            entry.getValue().put("curriculum_classtimeId", classtimeId.getColumnValue());

        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, classtimeTable, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, classtimeTable, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void testUpdateClasstimeType() {
        List<WideTable> allWideTables = getAllWideTables();
        List<EventData> dataList = getClasstimeTypeUpdate();
        classtimeTypeName.setUpdate(true);

        Pipeline pipeline = getPipeline();

        doReturn(new HashMap<String, Map<String, Object>>() {
            {
                //es exist values :
                put(cucId.getColumnValue(), new HashMap<String, Object>() {{

                    put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
                    put("curriculum_classtimeTypeId", classtimeTypeId.getColumnValue());
                    put("classtimeType_name", "over");


                }});
            }
        }).when(indexService).getDataMapByIds(Mockito.eq("curriculum"), Mockito.anyString(), Mockito.any(BoolQueryBuilder.class), Mockito.anyList(), Mockito.anyString());

        //Exe Values For check it ;
        operateWideIdMap.entrySet().forEach(entry -> {

            entry.getValue().put(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME, cucId.getColumnValue());
            entry.getValue().put("curriculum_classtimeTypeId", classtimeTypeId.getColumnValue());

        });

        operateWideIdMap.keySet().forEach(key -> {
            operateWideIdMap.get(key).keySet().forEach(vv -> {
//                //System.err.println(vv + "=" + operateWideIdMap.get(key).get(vv));
            });
        });

        doReturn(new int[]{1}).when(indexService).batchUpdateByIds(index_name, index_type, operateWideIdMap, null);
        int[] ints = eventDataIndexService.batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, classtimeType, pipeline, OperateType.UPDATE);
        verify(eventDataIndexService, times(1)).batchWideUpdateBySlave(index_name, index_type, dataList, allWideTables, classtimeType, pipeline, OperateType.UPDATE);
        Assert.assertTrue(ints.length == 1);
        Assert.assertTrue(ints[0] == 1);
    }

    @Test
    public void aVoid() {
        Lists.newArrayList("1", "2").forEach(d -> {
            if (d.equals("1")) {
                return;
            }

            System.out.println(d);
        });
    }

}
