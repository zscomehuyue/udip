package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.otter.common.push.index.es.EsIndexServiceImpl;
import com.alibaba.otter.common.push.index.es.MappingServiceImpl;
import com.alibaba.otter.common.push.index.wide.config.IndexConfigServiceFactory;
import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.config.CurriculumWideIndexConstants;
import com.alibaba.otter.shared.common.model.config.data.*;
import com.alibaba.otter.shared.common.model.config.data.es.IndexMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.DateUtils;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.collections.Lists;

import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.when;

public class BaseDataServiceMock {

    @Mock
    protected EsIndexServiceImpl indexService;

    @InjectMocks
    protected EventDataIndexService eventDataIndexService;

    @Mock
    protected IndexConfigServiceFactory indexConfigServiceFactory;

    protected HashMap<String, Object> fixField = new HashMap<String, Object>() {{
        put(indexConfigServiceFactory.ES_DATE, DateUtils.nowStr());
        put(indexConfigServiceFactory.ES_DATE_TIME, OffsetDateTime.now().toEpochSecond());
    }};
    protected String index_name = "clazz";
    protected String index_type = "udip";
    protected EventData eventData;
    protected Map<String, Map<String, Object>> operateWideIdMap;
    protected List<String> ids;

    protected List<EventData> prepareData() {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "clazz";
        index_type = "udip";
        EventColumn classPkid = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("clazz_id_01")
                .isNull(false)
                .build();
        EventColumn className = EventColumn.builder()
                .columnName("name")
                .columnType(Types.VARCHAR)
                .columnValue("class_name")
                .isNull(false)
                .build();
        EventColumn classYear = EventColumn.builder()
                .columnName("year")
                .columnType(Types.VARCHAR)
                .columnValue("2018")
                .isNull(false)
                .build();

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName(index_name)
                .keys(Lists.newArrayList(
                        classPkid
                ))
                .columns(Lists.newArrayList(
                        className,
                        classYear
                ))
                .build();
        ids = Lists.newArrayList(classPkid.getColumnValue());
        operateWideIdMap = new HashMap<String, Map<String, Object>>() {
            {
                put(classPkid.getColumnValue(), new HashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, classPkid.getColumnName()), classPkid.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, className.getColumnName()), className.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, classYear.getColumnName()), classYear.getColumnValue());
                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));
                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
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
     * insert into `retl`.`retl_buffer` ( `GMT_CREATE`, `PK_DATA`, `TABLE_ID`, `TYPE`, `FULL_NAME`, `GMT_MODIFIED`) values
     * ( '2018-08-08 17:10:23', '0000000049571bfa01495a55106e02b7', '0', 'I', 'otter.tb_class', '2018-08-08 17:10:23');
     */
    EventColumn cucId;
    EventColumn cucClassId;
    EventColumn classtimeTypeId;
    EventColumn classTimeId;
    EventColumn changeIn;
    EventColumn changeOut;

    protected List<EventData> getAddDatas(final int changeOutValue, final int changeInValue) {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";
        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();
        cucClassId = EventColumn.builder()
                .columnName("class_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classId_01")
                .isNull(false)
                .build();
        classtimeTypeId = EventColumn.builder()
                .columnName("classtime_type_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classtimeTypeId_01")
                .isNull(false)
                .build();
        classTimeId = EventColumn.builder()
                .columnName("classtime_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classtimeId_01")
                .isNull(false)
                .build();
        if (changeInValue > 0) {
            changeIn = EventColumn.builder()
                    .columnName("changein_course_num")
                    .columnType(Types.BIGINT)
                    .columnValue(String.valueOf(changeInValue))
                    .isNull(false)
                    .build();
        }

        if (changeOutValue > 0) {
            changeOut = EventColumn.builder()
                    .columnName("changeout_course_num")
                    .columnType(Types.BIGINT)
                    .columnValue(String.valueOf(changeOutValue))
                    .isNull(false)
                    .build();
        }
//        EventColumn clazzLevelId = EventColumn.builder()
//                .columnName("level_id")
//                .columnType(Types.VARCHAR)
//                .columnValue("clazz_levelId_01")
//                .isNull(false)
//                .mediaSource();
//        EventColumn clazzServicecenterId = EventColumn.builder()
//                .columnName("servicecenter_id")
//                .columnType(Types.VARCHAR)
//                .columnValue("clazz_servicecenterId_01")
//                .isNull(false)
//                .mediaSource();

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName(index_name)
                .keys(Lists.newArrayList(
                        cucId
                ))
                .columns(Lists.newArrayList(
                        cucId,
                        cucClassId,
                        classtimeTypeId,
                        classTimeId,
                        changeOut,
                        changeIn
                )).build();
        ids = Lists.newArrayList(cucId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, cucId.getColumnName()), cucId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, cucClassId.getColumnName()), cucClassId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, classtimeTypeId.getColumnName()), classtimeTypeId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, classTimeId.getColumnName()), classTimeId.getColumnValue());
                        if (null != changeIn) {
                            put(indexConfigServiceFactory.getDefaultFormatField(index_name, changeIn.getColumnName()), Long.valueOf(changeIn.getColumnValue()));
                        }
                        if (null != changeOut) {
                            put(indexConfigServiceFactory.getDefaultFormatField(index_name, changeOut.getColumnName()), Long.valueOf(changeOut.getColumnValue()));
                        }
                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));

                        put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);
                        put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
                        put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);


                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
    }

    protected List<EventData> getWideUpdate(final long changeOutValue, final long changeInValue) {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";
        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();
        cucClassId = EventColumn.builder()
                .columnName("class_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classId_01")
                .isNull(false)
                .build();
        classtimeTypeId = EventColumn.builder()
                .columnName("classtime_type_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classtimeTypeId_01")
                .isNull(false)
                .build();
        classTimeId = EventColumn.builder()
                .columnName("classtime_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classtimeId_01")
                .isNull(false)
                .build();
        if (changeInValue > 0) {
            changeIn = EventColumn.builder()
                    .columnName("changein_course_num")
                    .columnType(Types.BIGINT)
                    .columnValue(String.valueOf(changeInValue))
                    .isNull(false)
                    .isUpdate(true)
                    .build();
        }

        if (changeOutValue > 0) {
            changeOut = EventColumn.builder()
                    .columnName("changeout_course_num")
                    .columnType(Types.BIGINT)
                    .columnValue(String.valueOf(changeOutValue))
                    .isNull(false)
                    .isUpdate(true)
                    .build();
        }
        if (changeOutValue == -100l) {
            changeOut = EventColumn.builder()
                    .columnName("ignoreField")
                    .columnType(Types.BIGINT)
                    .columnValue(String.valueOf(changeOutValue))
                    .isNull(false)
                    .isUpdate(true)
                    .build();
        }

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName(index_name)
                .keys(Lists.newArrayList(
                        cucId
                ))
                .columns(Lists.newArrayList(
                        cucId,
                        cucClassId,
                        classtimeTypeId,
                        classTimeId
                )).build();
        if (changeInValue > 0) {
            eventData.getColumns().add(changeIn);
        }
        if (changeOutValue > 0 || changeOutValue == -100l) {
            eventData.getColumns().add(changeOut);
        }
        ids = Lists.newArrayList(cucId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, cucId.getColumnName()), cucId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, cucClassId.getColumnName()), cucClassId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, classtimeTypeId.getColumnName()), classtimeTypeId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(index_name, classTimeId.getColumnName()), classTimeId.getColumnValue());
                        if (null != changeIn) {
                            put(indexConfigServiceFactory.getDefaultFormatField(index_name, changeIn.getColumnName()), Long.valueOf(changeIn.getColumnValue()));
                        }
                        if (null != changeOut) {
                            put(indexConfigServiceFactory.getDefaultFormatField(index_name, changeOut.getColumnName()), Long.valueOf(changeOut.getColumnValue()));
                        }
                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));

//                        put(indexConfigServiceFactory.ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);
                        put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
                        put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);


                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
    }

    EventColumn maxPersonColumn;
    EventColumn classlevelId;
    EventColumn classDepartmentId;

    protected List<EventData> getClassUpdate(Long maxPerson) {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";

        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();


        cucClassId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("classId_01")
                .isNull(false)
                .build();
        maxPersonColumn = EventColumn.builder()
                .columnName("max_persons")
                .columnType(Types.BIGINT)
                .columnValue("" + maxPerson)
                .isNull(false)
                .build();
        classlevelId = EventColumn.builder()
                .columnName("level_id")
                .columnType(Types.VARCHAR)
                .columnValue("classlevelId_01")
                .isNull(false)
                .build();
        classDepartmentId = EventColumn.builder()
                .columnName("servicecenter_id")
                .columnType(Types.VARCHAR)
                .columnValue("departmentId_01")
                .isNull(false)
                .build();

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName("clazz")
                .keys(Lists.newArrayList(
                        cucClassId
                ))
                .columns(Lists.newArrayList(
                        cucClassId,
                        classDepartmentId,
                        maxPersonColumn,
                        classlevelId
                )).build();

        ids = Lists.newArrayList(cucClassId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {   //FIXME add wide index
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField("clazz", cucClassId.getColumnName()), cucClassId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField("clazz", maxPersonColumn.getColumnName()), Long.valueOf(maxPersonColumn.getColumnValue()));
                        put(indexConfigServiceFactory.getDefaultFormatField("clazz", classDepartmentId.getColumnName()), classDepartmentId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField("clazz", classlevelId.getColumnName()), classlevelId.getColumnValue());

                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));


                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
    }

    EventColumn departmentId;
    EventColumn districtId;

    protected List<EventData> getDepartUpdate() {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";

        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();
        departmentId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("departmentId_01")
                .isNull(false)
                .build();
        districtId = EventColumn.builder()
                .columnName("district_id")
                .columnType(Types.VARCHAR)
                .columnValue("district_id_01")
                .isNull(false)
                .build();

        classDepartmentId = EventColumn.builder()
                .columnName("servicecenter_id")
                .columnType(Types.VARCHAR)
                .columnValue("departmentId_01")
                .isNull(false)
                .build();

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName("department")
                .keys(Lists.newArrayList(
                        departmentId
                ))
                .columns(Lists.newArrayList(
                        districtId,
                        departmentId
                )).build();

        ids = Lists.newArrayList(departmentId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {   //FIXME add wide index
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField("department", departmentId.getColumnName()), departmentId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField("department", districtId.getColumnName()), districtId.getColumnValue());

                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));


                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
    }

    EventColumn classtimeId;
    EventColumn timeNameId;

    protected List<EventData> getClasstimeUpdate() {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";

        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();

        classtimeId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("departmentId_01")
                .isNull(false)
                .build();

        timeNameId = EventColumn.builder()
                .columnName("time_name")
                .columnType(Types.VARCHAR)
                .columnValue("time_name_01")
                .isNull(false)
                .build();


        final String classtime = "classtime";

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName(classtime)
                .keys(Lists.newArrayList(
                        classtimeId
                ))
                .columns(Lists.newArrayList(
                        classtimeId,
                        timeNameId
                )).build();

        ids = Lists.newArrayList(classtimeId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {   //FIXME add wide index
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField(classtime, classtimeId.getColumnName()), classtimeId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(classtime, timeNameId.getColumnName()), timeNameId.getColumnValue());

                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));


                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
    }


    EventColumn classtimeTypeName;

    protected List<EventData> getClasstimeTypeUpdate() {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";

        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();

        classtimeTypeId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("departmentId_01")
                .isNull(false)
                .build();

        classtimeTypeName = EventColumn.builder()
                .columnName("name")
                .columnType(Types.VARCHAR)
                .columnValue("time_name_01")
                .isNull(false)
                .build();


        final String classtime = "classtime_type";

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName(classtime)
                .keys(Lists.newArrayList(
                        classtimeTypeId
                ))
                .columns(Lists.newArrayList(
                        classtimeTypeId,
                        classtimeTypeName
                )).build();

        ids = Lists.newArrayList(classtimeTypeId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {   //FIXME add wide index
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField(classtime, classtimeTypeId.getColumnName()), classtimeTypeId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField(classtime, classtimeTypeName.getColumnName()), classtimeTypeName.getColumnValue());

                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));


                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
    }




    EventColumn registId;
    EventColumn registCountColumn;

    protected List<EventData> getRegistCount(long registCount) {
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);
        index_name = "curriculum";
        index_type = "udip";
        cucId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_id_01")
                .isNull(false)
                .build();

        registId = EventColumn.builder()
                .columnName("id")
                .columnType(Types.VARCHAR)
                .columnValue("class_regist_count_id_01")
                .isNull(false)
                .build();
        cucClassId = EventColumn.builder()
                .columnName("class_id")
                .columnType(Types.VARCHAR)
                .columnValue("curriculum_classId_01")
                .isNull(false)
                .build();
        registCountColumn = EventColumn.builder()
                .columnName("regist_count")
                .columnType(Types.BIGINT)
                .columnValue(String.valueOf(registCount))
                .isNull(false)
                .build();

        eventData = EventData.builder()
                .tableId(1)
                .schemaName("xxgl")
                .tableName("class_regist_count")
                .keys(Lists.newArrayList(
                        registId
                ))
                .columns(Lists.newArrayList(
                        registId, registCountColumn, cucClassId
                )).build();


        ids = Lists.newArrayList(registId.getColumnValue());
        operateWideIdMap = new ConcurrentHashMap<String, Map<String, Object>>() {
            {
                put(cucId.getColumnValue(), new ConcurrentHashMap<String, Object>() {
                    {
                        put(indexConfigServiceFactory.getDefaultFormatField("class_regist_count", registId.getColumnName()), registId.getColumnValue());
                        put(indexConfigServiceFactory.getDefaultFormatField("class_regist_count", registCountColumn.getColumnName()), Long.valueOf(registCountColumn.getColumnValue()));

                        put(indexConfigServiceFactory.getDefaultFormatField("class_regist_count", cucClassId.getColumnName()), cucClassId.getColumnValue());

                        put(indexConfigServiceFactory.ES_DATE, fixField.get(indexConfigServiceFactory.ES_DATE));
                        put(indexConfigServiceFactory.ES_DATE_TIME, fixField.get(indexConfigServiceFactory.ES_DATE_TIME));

                    }
                });
            }
        };

        //assemble service
        assembleService();
        return Lists.newArrayList(eventData);
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
     * insert into `retl`.`retl_buffer` ( `GMT_CREATE`, `PK_DATA`, `TABLE_ID`, `TYPE`, `FULL_NAME`, `GMT_MODIFIED`) values
     * ( '2018-08-08 17:10:23', '0000000049571bfa01495a55106e02b7', '0', 'I', 'otter.tb_class', '2018-08-08 17:10:23');
     */
    DataMedia<DataMediaSource> esIndex;
    DataMedia<DataMediaSource> rgseIndex;
    WideTable registCountTabel;
    WideTable clazzWideTable;
    WideTable deparetTable;
    WideTable classtimeTable;
    WideTable classlevelTable;
    WideTable classtimeType;

    protected List<WideTable> getAllWideTables() {
        List<WideTable> allWideTables = Lists.newArrayList();
        IndexMediaSource es = new IndexMediaSource();
        es.setClusterNodes("127.0.0.1");
        es.setClusterName("es");
        es.setType(DataMediaType.ES);

        DataMedia<DataMediaSource> mainTable = DataMedia.builder()
                .id(26L)
                .namespace("otter")
                .name("curriculum")
                .source(es)
                .build();
        DataMedia<DataMediaSource> rgseMainTable = DataMedia.builder()
                .id(100L)
                .namespace("otter")
                .name("rgse")
                .source(es)
                .build();

        esIndex = DataMedia.builder()
                .id(1L)
                .namespace("curriculum")
                .name("udip")
                .source(es)
                .build();
        rgseIndex = DataMedia.builder()
                .id(1L)
                .namespace("rgse")//regist_stage
                .name("udip")
                .source(es)
                .build();
        clazzWideTable = WideTable.builder()
                .id(1L)
                .target(esIndex)
                .wideTableName("curriculum")
                .mainTable(mainTable)
                .slaveTable(DataMedia.builder()
                        .id(2L)
                        .namespace("otter")
                        .name("clazz")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("curriculum_id")
                .slaveTablePkIdName("clazz_id")
                .slaveTableFkIdName("clazz_id")
                .mainTableFkIdName("curriculum_classId")
                .slaveMainTablePkIdName(null)
                .build();
        allWideTables.add(clazzWideTable);

          classtimeType = WideTable.builder()
                .id(2L)
                .target(esIndex)
                .wideTableName("curriculum")
                .mainTable(mainTable)
                .slaveTable(DataMedia.builder()
                        .id(18L)
                        .namespace("otter")
                        .name("classtime_type")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("curriculum_id")
                .slaveTablePkIdName("classtimeType_id")
                .slaveTableFkIdName("classtimeType_id")
                .mainTableFkIdName("curriculum_classtimeTypeId")
                .slaveMainTablePkIdName(null)
                .build();
        allWideTables.add(classtimeType);

        registCountTabel = WideTable.builder()
                .id(3L)
                .target(esIndex)
                .wideTableName("curriculum")
                .mainTable(mainTable)
                .slaveTable(DataMedia.builder()
                        .id(16L)
                        .namespace("otter")
                        .name("class_regist_count")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("curriculum_id")
                .slaveTablePkIdName("classRegistCount_id")
                .slaveTableFkIdName("classRegistCount_classId")
                .mainTableFkIdName("curriculum_classId")
                .slaveMainTablePkIdName(null)
                .build();
        allWideTables.add(registCountTabel);

        classtimeTable = WideTable.builder()
                .id(4L)
                .target(esIndex)
                .wideTableName("curriculum")
                .mainTable(mainTable)
                .slaveTable(DataMedia.builder()
                        .id(20L)
                        .namespace("otter")
                        .name("classtime")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("curriculum_id")
                .slaveTablePkIdName("classtime_id")
                .slaveTableFkIdName("classtime_id")
                .mainTableFkIdName("curriculum_classtimeId")
                .slaveMainTablePkIdName(null)
                .build();
        allWideTables.add(classtimeTable);

          classlevelTable = WideTable.builder()
                .id(5L)
                .target(esIndex)
                .wideTableName("curriculum")
                .mainTable(mainTable)
                .slaveTable(DataMedia.builder()
                        .id(22L)
                        .namespace("otter")
                        .name("classlevel")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("curriculum_id")
                .slaveTablePkIdName("classlevel_id")
                .slaveTableFkIdName("classlevel_id")
                .mainTableFkIdName("clazz_levelId")
                .slaveMainTablePkIdName(null)
                .build();
        allWideTables.add(classlevelTable);

        deparetTable = WideTable.builder()
                .id(6L)
                .target(esIndex)
                .wideTableName("curriculum")
                .mainTable(mainTable)
                .slaveTable(DataMedia.builder()
                        .id(24L)
                        .namespace("otter")
                        .name("department")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("curriculum_id")
                .slaveTablePkIdName("department_id")
                .slaveTableFkIdName("department_id")
                .mainTableFkIdName("clazz_servicecenterId")
                .slaveMainTablePkIdName(null)
                .build();
        allWideTables.add(deparetTable);


        //FIXME add REGIS_STAGE

        return allWideTables;
    }

    protected List<WideTable> getRegistStageWideTables() {
        List<WideTable> allWideTables = Lists.newArrayList();
        IndexMediaSource es = new IndexMediaSource();
        es.setClusterNodes("127.0.0.1");
        es.setClusterName("es");
        es.setType(DataMediaType.ES);

        DataMedia<DataMediaSource> rgseMainTable = DataMedia.builder()
                .id(100L)
                .namespace("otter")
                .name("rgse")
                .source(es)
                .build();

        rgseIndex = DataMedia.builder()
                .id(1L)
                .namespace("rgse")//regist_stage
                .name("udip")
                .source(es)
                .build();

        allWideTables.add(WideTable.builder()
                .id(7L)
                .target(rgseIndex)
                .wideTableName("rgse")
                .mainTable(rgseMainTable)
                .slaveTable(DataMedia.builder()
                        .id(25L)
                        .namespace("otter")
                        .name("stu")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("rgse_id")
                .slaveTablePkIdName("stu_id")
                .slaveTableFkIdName("stu_id")
                .mainTableFkIdName("rgse_studentId")
                .slaveMainTablePkIdName(null)
                .build());

        allWideTables.add(WideTable.builder()
                .id(8L)
                .target(rgseIndex)
                .wideTableName("rgse")
                .mainTable(rgseMainTable)
                .slaveTable(DataMedia.builder()
                        .id(26L)
                        .namespace("otter")
                        .name("rg")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("rgse_id")
                .slaveTablePkIdName("rg_id")
                .slaveTableFkIdName("rg_id")
                .mainTableFkIdName("rgse_registId")
                .slaveMainTablePkIdName(null)
                .build());

        allWideTables.add(WideTable.builder()
                .id(10L)
                .target(rgseIndex)
                .wideTableName("rgse")
                .mainTable(rgseMainTable)
                .slaveTable(DataMedia.builder()
                        .id(28L)
                        .namespace("otter")
                        .name("clase")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("rgse_id")
                .slaveTablePkIdName("clase_id")
                .slaveTableFkIdName("clase_id")
                .mainTableFkIdName("rgse_classStageId")
                .slaveMainTablePkIdName(null)
                .build());

        allWideTables.add(WideTable.builder()
                .id(11L)
                .target(rgseIndex)
                .wideTableName("rgse")
                .mainTable(rgseMainTable)
                .slaveTable(DataMedia.builder()
                        .id(2L)
                        .namespace("otter")
                        .name("clazz")
                        .source(mediaSource)
                        .build())
                .mainTablePkIdName("rgse_id")
                .slaveTablePkIdName("clazz_id")
                .slaveTableFkIdName("clazz_id")
                .mainTableFkIdName("rgse_classId")
                .slaveMainTablePkIdName(null)
                .build());

        return allWideTables;

    }

    protected String cloneWidetables() {
        return JsonUtils.marshalToString(getAllWideTables());
    }

    protected String clonePipeline() {
        return JsonUtils.marshalToString(getPipeline());
    }


    DataMediaSource mediaSource = DataMediaSource.builder().type(DataMediaType.ES).build();

    protected Pipeline getPipeline() {
        Pipeline pipeline = Pipeline.builder()
                .id(1L)
                .channelId(1L)
                .name("pipeline test")
                .routes(Lists.newArrayList())
                .build();
        getAllWideTables().forEach(wideTable -> {
            pipeline.getRoutes().add(LoadRoute.builder()
                    .table(wideTable.getSlaveTable())
                    .loadDataMedia(DataMedia.builder().namespace(wideTable.getSlaveTable().getName()).source(mediaSource).name("udip").build())
                    .type(LoadType.SINGLE_INDEX)
                    .build());
            pipeline.getRoutes().add(LoadRoute.builder()
                    .table(wideTable.getMainTable())
                    .loadDataMedia(wideTable.getTarget())
                    .type(LoadType.SINGLE_INDEX)
                    .build());
        });

        return pipeline;
    }

    protected void assembleService() {
        indexService = Mockito.spy(EsIndexServiceImpl.class);
        MappingServiceImpl mappingService = new MappingServiceImpl();
        try {
            mappingService.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        indexService.setMappingService(mappingService);
        indexConfigServiceFactory = Mockito.spy(IndexConfigServiceFactory.class);

        eventDataIndexService = Mockito.spy(EventDataIndexService.class);
        eventDataIndexService.setIndexService(indexService);
        eventDataIndexService.setIndexConfigServiceFactory(indexConfigServiceFactory);
        when(indexConfigServiceFactory.getFixFieldMap(Mockito.anyString(), Mockito.eq(OperateType.UPDATE))).thenReturn(fixField);
        when(indexConfigServiceFactory.getFixFieldMap(Mockito.anyString(), Mockito.eq(OperateType.INSERT))).thenReturn(fixField);
    }

}
