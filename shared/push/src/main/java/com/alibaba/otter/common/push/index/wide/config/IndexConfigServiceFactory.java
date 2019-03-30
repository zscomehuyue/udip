package com.alibaba.otter.common.push.index.wide.config;

import com.alibaba.otter.common.push.index.type.OperateType;
import com.alibaba.otter.common.push.index.wide.*;
import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.common.utils.DateUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.common.utils.NameFormatUtils;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.alibaba.otter.shared.common.utils.LogUtils.ERROR;
import static com.alibaba.otter.shared.common.utils.LogUtils.INFO;

public class IndexConfigServiceFactory {
    private static final Logger log = LoggerFactory.getLogger(IndexConfigServiceFactory.class);

    public static final String table_field_sufix = "_";
    public static final String ES_STATUS = "esStatus";
    public static final String ES_DATE = "esDate";
    public static final String ES_DATE_TIME = "esDateTime";

    private Map<String, Object> getDefaultIndexFiledMap(OperateType type) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(ES_DATE, DateUtils.nowStr());
        map.put(ES_DATE_TIME, DateUtils.nowSecond());
        return map;
    }

    private Map<String, FieldFormatService> fieldFormateMap = new HashMap<String, FieldFormatService>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.TEACHER_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.SYSTEM_PARAM_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.DISTRICT_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.DEPARTMENT_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.CLASSTIME_TYPE_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.CLASSTIME_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.CLASSLEVEL_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(CurriculumWideIndexConstants.CHANGE_COURSE_AMOUNT_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(RegistStageWideIndexConstants.REGIST_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(RegistStageWideIndexConstants.STUDENT_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(RegistStageWideIndexConstants.CLASS_STAGE_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, (tableName, fileName) -> getDefaultFormatField(tableName, fileName));
    }};

    private Map<String, ValidFieldService> dirtyFieldMap = new HashMap<String, ValidFieldService>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, (map, wideTables) -> {
            boolean first = wideTables.stream().filter(wideTable -> {
                if (null == map.get(wideTable.getMainTableFkIdName()) || null == map.get(wideTable.getSlaveTableFkIdName())) {
                    LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index curriculum MainTableFkIdName:%s ,SlaveTableFkIdName:%s is null id:%s ", wideTable.getMainTableFkIdName(), wideTable.getSlaveTableFkIdName(), map.get("curriculum_id"));
                    return true;
                }
                if (!map.get(wideTable.getMainTableFkIdName()).equals(map.get(wideTable.getSlaveTableFkIdName()))) {
                    LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index curriculum fkid is not same id:%s ", map.get("curriculum_id"));
                    return true;
                }
                return false;
            }).findFirst().isPresent();
            if (first) {
                return true;
            }
            if (null != map.get(ES_STATUS)
                    && map.get(ES_STATUS).toString().equals(String.valueOf(FieldHelper.ES_SYNC_WIDE_INDEX_INIT))) {
                LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index curriculum esStatus is 0  id:%s ", map.get("curriculum_id"));
                return true;
            }
            try {
                int value = Integer.parseInt(map.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS).toString())
                        - Integer.parseInt(map.get(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT) == null ? "0" : map.get(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT).toString())
                        + Integer.parseInt(map.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM) == null ? "0" : map.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM).toString())
                        - Integer.parseInt(map.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM) == null ? "0" : map.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM).toString());
                if (value != Integer.parseInt(map.get(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT).toString())) {
                    LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index curriculum handle special field execRemainCount:%s ,remainCount:%s  , id:%s ", value
                            , Integer.parseInt(map.get(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT).toString()), map.get(CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME));
                    return true;
                }
            } catch (Exception e) {
                LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>error:%s ", e);
            }
            return false;
        });
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, (map, wideTables) -> {
            boolean first = wideTables.stream().filter(wideTable -> {
                if (null == map.get(wideTable.getMainTableFkIdName()) || null == map.get(wideTable.getSlaveTableFkIdName())) {
                    LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index rgse MainTableFkIdName:%s ,SlaveTableFkIdName:%s is null id:%s ", wideTable.getMainTableFkIdName(), wideTable.getSlaveTableFkIdName(), map.get("rgse_id"));
                    return true;
                }
                if (!map.get(wideTable.getMainTableFkIdName()).equals(map.get(wideTable.getSlaveTableFkIdName()))) {
                    LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index rgse fkid is not same id:%s ", map.get("rgse_id"));
                    return true;
                }
                return false;
            }).findFirst().isPresent();
            if (first) {
                return true;
            }
            if (null != map.get(FieldMapping.WIDE_TABLE_SYNC_FIELD_ES_STATUS)
                    && map.get(FieldMapping.WIDE_TABLE_SYNC_FIELD_ES_STATUS).toString().equals(String.valueOf(FieldHelper.ES_SYNC_WIDE_INDEX_INIT))) {
                LogUtils.log(INFO, log, () -> "=dirtyFieldMap=>index rgse esStatus is 0  id:%s ", map.get("rgse_id"));
                return true;
            }
            return false;
        });
    }};

    private Map<String, String> pkidFormateMap = new HashMap<String, String>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CURRICULUM_WIDE_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.TEACHER_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.TEACHER_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.SYSTEM_PARAM_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.SYSTEM_PARAM_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.DISTRICT_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.DISTRICT_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.DEPARTMENT_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.CLASSTIME_TYPE_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.CLASSTIME_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.CLASSLEVEL_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME);
        put(CurriculumWideIndexConstants.CHANGE_COURSE_AMOUNT_TABLE_OR_INDEX_NAME, CurriculumWideIndexConstants.CHANGE_COURSE_AMOUNT_INDEX_PKID_NAME);
        put(RegistStageWideIndexConstants.REGIST_TABLE_OR_INDEX_NAME, RegistStageWideIndexConstants.REGIST_PKID_NAME);
        put(RegistStageWideIndexConstants.STUDENT_TABLE_OR_INDEX_NAME, RegistStageWideIndexConstants.STUDENT_PKID_NAME);
        put(RegistStageWideIndexConstants.CLASS_STAGE_TABLE_OR_INDEX_NAME, RegistStageWideIndexConstants.CLASE_PKID_NAME);
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, RegistStageWideIndexConstants.REGIST_STAGE_PKID_NAME);
    }};

    private Map<String, List<String>> needUpdateFields = new HashMap<String, List<String>>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, Lists.newArrayList(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM, CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM, "curriculum_classId", CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT, CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));
        put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, Lists.newArrayList(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));
        put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME, Lists.newArrayList(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT));
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, foramtFields(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, FieldMapping.RGSE_REGIST_STAGE_INCLUDE_FIELD.values()));

    }};

    public List<String> foramtFields(String index, Collection<String> targetFields) {
        return targetFields.stream().map(field -> getDefaultFormatField(index, field)).collect(Collectors.toList());
    }

    private Map<String, List<String>> slaveTableAddedForUpdateWide = new HashMap<String, List<String>>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(
                        CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME
                        , CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME));

        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, Lists.newArrayList(RegistStageWideIndexConstants.REGIST_TABLE_OR_INDEX_NAME));
    }};

    //FIXME add need dynamic index filed and special field
    private Map<String, List<String>> tableNeedField = new HashMap<String, List<String>>() {{
        put(NameFormatUtils.formatName(CurriculumWideIndexConstants.CLASSLEVEL_TABLE_OR_INDEX_NAME),
                Lists.newArrayList(
                        CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
//                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));

        put(NameFormatUtils.formatName(CurriculumWideIndexConstants.CLASSTIME_TABLE_OR_INDEX_NAME),
                Lists.newArrayList(
                        CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
//                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));

        put(NameFormatUtils.formatName(CurriculumWideIndexConstants.CLASSTIME_TYPE_TABLE_OR_INDEX_NAME),
                Lists.newArrayList(
                        CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
//                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));

        put(NameFormatUtils.formatName(CurriculumWideIndexConstants.DEPARTMENT_TABLE_OR_INDEX_NAME),
                Lists.newArrayList(
                        CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
//                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));

        put(NameFormatUtils.formatName(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME),
                Lists.newArrayList(
                        CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));
        put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT));
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM
                        , CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT
                        , ES_STATUS
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS));

        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_CITY_ID_NAME
                        , RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_YEAR_NAME
                        , ES_STATUS
                        , RegistStageWideIndexConstants.REGIST_PKID_NAME
                        , RegistStageWideIndexConstants.STUDENT_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , RegistStageWideIndexConstants.CLASE_PKID_NAME));

        put(RegistStageWideIndexConstants.REGIST_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_CITY_ID_NAME
                        , RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_YEAR_NAME
                        , ES_STATUS
                        , RegistStageWideIndexConstants.REGIST_PKID_NAME
                        , RegistStageWideIndexConstants.STUDENT_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , RegistStageWideIndexConstants.CLASE_PKID_NAME));

        put(RegistStageWideIndexConstants.STUDENT_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_CITY_ID_NAME
                        , RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_YEAR_NAME
                        , ES_STATUS
                        , RegistStageWideIndexConstants.STUDENT_PKID_NAME
                        , RegistStageWideIndexConstants.REGIST_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , RegistStageWideIndexConstants.CLASE_PKID_NAME));

        put(RegistStageWideIndexConstants.CLASS_STAGE_TABLE_OR_INDEX_NAME,
                Lists.newArrayList(RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_CITY_ID_NAME
                        , RegistStageWideIndexConstants.REGIST_STAGE_DYNAMIC_FIELD_YEAR_NAME
                        , ES_STATUS
                        , RegistStageWideIndexConstants.REGIST_PKID_NAME
                        , RegistStageWideIndexConstants.CLASE_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , RegistStageWideIndexConstants.STUDENT_PKID_NAME));

    }};

    /**
     * index ,tableName format it ,target fields
     */
    private Map<String, Map<String, Collection<String>>> wideIndexLinkSubTableField = new HashMap<String, Map<String, Collection<String>>>() {{

        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, new HashMap<String, Collection<String>>() {{
            put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, FieldMapping.CLASS_INCLUDE_FIELD.values());
            put(CurriculumWideIndexConstants.CLASSTIME_TABLE_OR_INDEX_NAME, FieldMapping.CLASS_TIME_INCLUDE_FIELD.values());
            put(NameFormatUtils.formatName(CurriculumWideIndexConstants.CLASSTIME_TYPE_TABLE_OR_INDEX_NAME), FieldMapping.CLASS_TIME_TYPE_INCLUDE_FIELD.values());
            put(CurriculumWideIndexConstants.CLASSLEVEL_TABLE_OR_INDEX_NAME, FieldMapping.CLASS_LEVEL_INCLUDE_FIELD.values());
            put(NameFormatUtils.formatName(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME), FieldMapping.REGISTER_COUNT_INCLUDE_FIELD.values());
            put(CurriculumWideIndexConstants.DEPARTMENT_TABLE_OR_INDEX_NAME, FieldMapping.DEPARTMENT_INCLUDE_FIELD.values());
            put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, FieldMapping.CURRICULUM_INCLUDE_FIELD.values());
        }});
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, new HashMap<String, Collection<String>>() {{
            put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, FieldMapping.CLASS_INCLUDE_FIELD.values());
            put(RegistStageWideIndexConstants.REGIST_TABLE_OR_INDEX_NAME, FieldMapping.RGSE_REGISTER_INCLUDE_FIELD.values());
            put(RegistStageWideIndexConstants.STUDENT_TABLE_OR_INDEX_NAME, FieldMapping.RGSE_STUDENT_INCLUDE_FIELD.values());
            put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, FieldMapping.RGSE_REGIST_STAGE_INCLUDE_FIELD.values());
            put(RegistStageWideIndexConstants.CLASS_STAGE_TABLE_OR_INDEX_NAME, FieldMapping.RGSE_CLASS_STAGE_INCLUDE_FIELD.values());
        }});

    }};

    public String getDefaultFormatField(String tableName, String fileName) {
        return NameFormatUtils.formatName(tableName) + table_field_sufix + NameFormatUtils.formatName(fileName);
    }

    private Map<String, DataMappingService> mappingServiceMap = new ConcurrentHashMap<String, DataMappingService>() {{
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, new DataMappingService() {
            private Map<String, Boolean> lockCache = Maps.newConcurrentMap();

            @Override
            public String createIndexWithMapping(String index, String dynamicIndex, TransportClient client) {
                if (!lockCache.getOrDefault(dynamicIndex, false)) {
                    synchronized (this) {
                        if (!lockCache.getOrDefault(dynamicIndex, false)) {
                            if (!client.admin().indices().exists(new IndicesExistsRequest(dynamicIndex)).actionGet().isExists()) {
                                try {
                                    //FIXME  fields
                                    XContentBuilder builder = XContentFactory.jsonBuilder();
                                    builder.startObject()
                                            .startObject("properties")
                                            .startObject("clazz_maxPersons").field("type", "long").endObject()
                                            .startObject("clase_updateTime").field("type", "date").endObject()
                                            .startObject("esDateTime").field("type", "long").endObject()
                                            .startObject("clazz_createType").field("type", "integer").endObject()
                                            .startObject("clazz_isClose").field("type", "long").endObject()
                                            .startObject("clazz_tutorId").field("type", "keyword").endObject()
                                            .startObject("rgse_cityId").field("type", "keyword").endObject()
                                            .startObject("rg_cityId").field("type", "keyword").endObject()
                                            .startObject("rg_id").field("type", "keyword").endObject()
                                            .startObject("rg_sourceClassId").field("type", "keyword").endObject()
                                            .startObject("rg_studentId").field("type", "keyword").endObject()
                                            .startObject("rgse_registId").field("type", "keyword").endObject()
                                            .startObject("rg_createId").field("type", "keyword").endObject()
                                            .startObject("rg_classId").field("type", "keyword").endObject()
                                            .startObject("clazz_passedCount").field("type", "long").endObject()
                                            .startObject("rgse_addCurriculumCount").field("type", "long").endObject()
                                            .startObject("clazz_startDate").field("type", "date").endObject()
                                            .startObject("rg_payEnddate").field("type", "date").endObject()
                                            .startObject("rg_createDate").field("type", "date").endObject()
                                            .startObject("rgse_curriculumCount").field("type", "long").endObject()
                                            .startObject("classStageName").field("type", "keyword").endObject()
                                            .startObject("rgse_classStageId").field("type", "keyword").endObject()
                                            .startObject("clazz_areaId").field("type", "keyword").endObject()
                                            .startObject("clazz_classTimeTypeId").field("type", "keyword").endObject()
                                            .startObject("rgse_studentId").field("type", "keyword").endObject()
                                            .startObject("stu_cityId").field("type", "keyword").endObject()
                                            .startObject("stu_loginname").field("type", "keyword").endObject()
                                            .startObject("clase_stagesNum").field("type", "long").endObject()
                                            .startObject("clazz_classCount").field("type", "long").endObject()
                                            .startObject("rgse_payTime").field("type", "date").endObject()
                                            .startObject("clazz_isHidden").field("type", "long").endObject()
                                            .startObject("clazz_isTest").field("type", "long").endObject()
                                            .startObject("clazz_bizType").field("type", "integer").endObject()
                                            .startObject("clazz_isSchooltest").field("type", "long").endObject()
                                            .startObject("clazz_venueId").field("type", "keyword").endObject()
                                            .startObject("clazz_isDelete").field("type", "long").endObject()
                                            .startObject("clase_cityId").field("type", "keyword").endObject()
                                            .startObject("clase_id").field("type", "keyword").endObject()
                                            .startObject("esDate").field("type", "keyword").endObject()
                                            .startObject("clazz_subjectLongValue").field("type", "keyword").endObject()
                                            .startObject("rgse_classId").field("type", "keyword").endObject()
                                            .startObject("clazz_year").field("type", "keyword").endObject()
                                            .startObject("rgse_deleted").field("type", "long").endObject()
                                            .startObject("rg_isNewstu").field("type", "integer").endObject()
                                            .startObject("esStatus").field("type", "integer").endObject()
                                            .startObject("stu_uid").field("type", "long").endObject()
                                            .startObject("clazz_isLiveClass").field("type", "long").endObject()
                                            .startObject("rgse_payed").field("type", "integer").endObject()
                                            .startObject("rg_way").field("type", "integer").endObject()
                                            .startObject("clazz_isDisplayFront").field("type", "long").endObject()
                                            .startObject("clazz_levelId").field("type", "keyword").endObject()
                                            .startObject("clazz_classType").field("type", "integer").endObject()
                                            .startObject("clazz_id").field("type", "keyword").endObject()
                                            .startObject("clazz_gradeId").field("type", "keyword").endObject()
                                            .startObject("stu_id").field("type", "keyword").endObject()
                                            .startObject("clazz_venueName").field("type", "keyword").endObject()
                                            .startObject("clazz_gradeTypeId").field("type", "keyword").endObject()
                                            .startObject("clazz_servicecenterId").field("type", "keyword").endObject()
                                            .startObject("rgse_id").field("type", "keyword").endObject()
                                            .startObject("clazz_endDate").field("type", "date").endObject()
                                            .startObject("clazz_cityId").field("type", "keyword").endObject()
                                            .startObject("clazz_coursewareSend").field("type", "long").endObject()
                                            .startObject("clazz_teacherIds").field("type", "keyword").endObject()
                                            .startObject("rgse_modifyTime").field("type", "date").endObject()
                                            .startObject("clazz_classtimeIds").field("type", "keyword").endObject()
                                            .startObject("clazz_isDisplayTeacher").field("type", "long").endObject()
                                            .startObject("clazz_isDoubleTeacherLiveClass").field("type", "long").endObject()
                                            .startObject("clazz_termId").field("type", "keyword").endObject()
                                            .startObject("clazz_price").field("type", "float").endObject()
                                            .endObject()
                                            .endObject();
                                    client.admin().indices().prepareCreate(dynamicIndex).setSettings(Settings.builder()
                                            .put("number_of_shards", 3)
                                            .put("number_of_replicas", 1).build()).addAlias(new Alias(index)).get();
                                    PutMappingRequest mappingRequest = Requests.putMappingRequest(dynamicIndex).type("udip").source(builder);
                                    client.admin().indices().putMapping(mappingRequest).actionGet();
                                    lockCache.put(dynamicIndex, true);
                                } catch (IOException e) {
                                    LogUtils.log(INFO, log, () -> "=mappingServiceMap=>index:%s ,create error:%s", dynamicIndex, e);
                                }
                            }
                        }

                    }
                }
                return "The index:" + dynamicIndex + " has created.";
            }
        });
    }};

    public String createIndexWithMapping(String index, String dynmaicIndex, TransportClient client) {
        return null == mappingServiceMap.get(index) ? "Not need create index:" + index + " and mapping." : mappingServiceMap.get(index).createIndexWithMapping(index, dynmaicIndex, client);
    }

    /**
     * 第一次添加宽表时 ；
     * classRegistCount添加时
     * <p>
     * 更新涉及到字段：classRegistCount_registCount ，curriculum_changeoutCourseNum ，curriculum_changeinCourseNum
     * 宽表更新，外键，而外键涉及到如上字段时；
     */
    private Map<String, SpecialFieldService> specialFieldMap = new HashMap<String, SpecialFieldService>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, (param, type) -> {
            HashMap<String, Object> filedFlag = new HashMap<>();
            if (null == param.get(ES_STATUS) || FieldHelper.ES_SYNC_WIDE_INDEX_INIT == Integer.parseInt(param.get(ES_STATUS).toString())) {
                if (null != param.get(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME)
                        && null != param.get(CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME)
//                        && null != param.get("classRegistCount_id")
                        && null != param.get(CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME)
                        && null != param.get(CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME)
                        && null != param.get(CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME)) {
                    filedFlag.put(ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);
                } else {
                    filedFlag.put(ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);
                }
            }

            if ((null != param.get(ES_STATUS) && FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED == Integer.parseInt(param.get(ES_STATUS).toString()))
                    || FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED == Integer.parseInt(filedFlag.get(ES_STATUS).toString())) {
                if (null != param.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS)) {
                    Integer value = Integer.parseInt(param.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS).toString())
                            - Integer.parseInt(null == param.get(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT) ? "0" : param.get(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT).toString())
                            + Integer.parseInt(null == param.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM) ? "0" : param.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_OUT_COURSE_NUM).toString())
                            - Integer.parseInt(null == param.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM) ? "0" : param.get(CurriculumWideIndexConstants.CURRICULUM_CHANGE_IN_COURSE_NUM).toString());
                    filedFlag.put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, value);
                }
                if (null != param.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS)) {
                    filedFlag.put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, Integer.parseInt(param.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS).toString())
                            - Integer.parseInt(param.get(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT) == null ? "0" : param.get(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_REGIST_COUNT).toString()));
                }
            } else {
                if (null == param.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS)) {
                    filedFlag.put(CurriculumWideIndexConstants.CURRICULUM_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
                }
                if (null == param.get(CurriculumWideIndexConstants.CLAZZ_MAX_PERSONS)) {
                    filedFlag.put(CurriculumWideIndexConstants.CURRICULUM_ORDER_REMAIN_COUNT, FieldHelper.REMAIN_COUNT_DEFAULT_VALUE);
                }
            }
            return filedFlag;
        });
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, (param, type) -> {
            HashMap<String, Object> filedFlag = new HashMap<>();
            if (null == param.get(ES_STATUS) || FieldHelper.ES_SYNC_WIDE_INDEX_INIT == Integer.parseInt(param.get(ES_STATUS).toString())) {
                if (null != param.get(RegistStageWideIndexConstants.REGIST_PKID_NAME)
                        && null != param.get(RegistStageWideIndexConstants.STUDENT_PKID_NAME)
                        && null != param.get(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME)
                        && null != param.get(RegistStageWideIndexConstants.CLASE_PKID_NAME)) {
                    filedFlag.put(ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);
                } else {
                    filedFlag.put(ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT);
                }
            }
            //转换成String类型存储
            if (null != param.get(CurriculumWideIndexConstants.CLAZZ_INDEX_SUBJECT_LONG_VALUE_NAME)) {
                param.put(CurriculumWideIndexConstants.CLAZZ_INDEX_SUBJECT_LONG_VALUE_NAME, param.get(CurriculumWideIndexConstants.CLAZZ_INDEX_SUBJECT_LONG_VALUE_NAME).toString());
            }
            return filedFlag;
        });


    }};

    private Map<String, List<String>> fkFieldOfWide = new HashMap<String, List<String>>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME
                , Lists.newArrayList(CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.DEPARTMENT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSLEVEL_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASS_REGIST_COUNT_INDEX_PKID_NAME
                        , CurriculumWideIndexConstants.CLASSTIME_TYPE_INDEX_PKID_NAME));

        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME
                , Lists.newArrayList(RegistStageWideIndexConstants.REGIST_PKID_NAME
                        , RegistStageWideIndexConstants.STUDENT_PKID_NAME
                        , RegistStageWideIndexConstants.CLASE_PKID_NAME
                        , CurriculumWideIndexConstants.CLAZZ_INDEX_PKID_NAME
                ));

    }};

    private Map<String, FixFieldService> fixFieldMap = new HashMap<String, FixFieldService>() {{
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.TEACHER_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.SYSTEM_PARAM_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.DISTRICT_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.DEPARTMENT_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.CLASSTIME_TYPE_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.CLASSTIME_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.CLASSLEVEL_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(CurriculumWideIndexConstants.CHANGE_COURSE_AMOUNT_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(RegistStageWideIndexConstants.REGIST_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(RegistStageWideIndexConstants.STUDENT_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(RegistStageWideIndexConstants.CLASS_STAGE_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
        put(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, type -> getDefaultIndexFiledMap(type));
    }};

    private Map<String, DynmaicIndexService> dynamicIndexdMap = new HashMap<String, DynmaicIndexService>() {{

    }};

    private Map<String, FieldTypeService> fieldTypeMap = new HashMap<String, FieldTypeService>() {{
        put(CurriculumWideIndexConstants.CLAZZ_TABLE_OR_INDEX_NAME, (FieldFormatService service, String tableName, EventColumn column) -> {
            String formatField = service.getFormatField(tableName, column.getColumnName());
            if (null != FieldMapping.CONVERTER_FIELD.get(formatField) && !column.isNull()) {
                return new HashMap<String, Object>() {{
                    put(formatField, column.getColumnValue().split("[|,]"));
                }};
            }
            return new HashMap<>(0);
        });
        //for clazz add last
        put(CurriculumWideIndexConstants.CURRICULUM_WIDE_TABLE_OR_INDEX_NAME, (FieldFormatService service, String tableName, EventColumn column) -> {
            String formatField = service.getFormatField(tableName, column.getColumnName());
            if (null != FieldMapping.CONVERTER_FIELD.get(formatField) && !column.isNull()) {
                return new HashMap<String, Object>() {{
                    put(formatField, column.getColumnValue().split("[|,]"));
                }};
            }
            return new HashMap<>(0);
        });

    }};

    public boolean slaveTableAddedIsNeedWide(String indexName, String targetTableName) {
        return slaveTableAddedForUpdateWide.get(indexName) == null ? Boolean.FALSE : slaveTableAddedForUpdateWide.get(indexName).contains(targetTableName);
    }

    public List<String> getNeedUpdateFields(String index) {
        return needUpdateFields.get(index) == null ? new ArrayList<>(0) : needUpdateFields.get(index);
    }

    public List<String> getFkFieldOfWide(String index) {
        return fkFieldOfWide.get(index) == null ? new ArrayList<>(0) : fkFieldOfWide.get(index);
    }

    public List<String> getTableNeedField(String slaveTableName) {
        return tableNeedField.get(slaveTableName) == null ? new ArrayList<>(0) : tableNeedField.get(slaveTableName);
    }

    public boolean isDirtyFieldMap(String index, Map<String, Object> dataMap, List<WideTable> wideTables) {
        return dirtyFieldMap.get(index) == null ? false : dirtyFieldMap.get(index).valid(dataMap, wideTables);
    }

    public Collection<String> getWideIndexLinkSubTableField(String index, String tableName) {
        return wideIndexLinkSubTableField.get(index) == null ? new ArrayList<>(0) :
                (wideIndexLinkSubTableField.get(index).get(tableName) == null ? new ArrayList<>(0)
                        : wideIndexLinkSubTableField.get(index).get(tableName));
    }

    public String getFieldFormateMap(String index, String tableName, String fieldName) {
        try {
            return fieldFormateMap.get(index).getFormatField(tableName, fieldName);
        } catch (Exception e) {
            LogUtils.log(ERROR, log, () -> "=getFieldFormateMap=>index:%s ,tableName:%s ,fieldName:%s ,error:%s", index, tableName, fieldName, e);
        }
        return "no_index";
    }

    public String getPkidFormateMap(String index) {
        return pkidFormateMap.get(index);
    }

    public Map<String, Map<String, Map<String, Object>>> getDynamicIndexdMap(String index, String type, OperateType eventType, Map<String, Map<String, Object>> pkidMap) {
        return dynamicIndexdMap.get(index) == null ? null : dynamicIndexdMap.get(index).getDynmaicDataMap(index, type, eventType, pkidMap);
    }

    public Map<String, List<String>> getDynamicIndexdMap(String index, String type, List<String> pkIds) {
        return dynamicIndexdMap.get(index) == null ? null : dynamicIndexdMap.get(index).getDynmaicDataMap(index, type, pkIds);
    }

    public Map<String, Object> getFixFieldMap(String index, OperateType type) {
        return fixFieldMap.get(index) == null ? new HashMap<>(0) : fixFieldMap.get(index).getFixFields(type);
    }

    public Map<String, Object> getSpecialFieldMap(String index, Map<String, Object> dataMap, OperateType type) {
        return specialFieldMap.get(index) == null ? new HashMap<>(0) : specialFieldMap.get(index).handleFields(dataMap, type);
    }

    public Map<String, Object> getFieldTypeMap(String index, String tableName, EventColumn column) {
        return fieldTypeMap.get(index) == null ? new HashMap<>(0) : fieldTypeMap.get(index).handleFieldType(fieldFormateMap.get(index), tableName, column);
    }

    public void setFieldFormateMap(Map<String, FieldFormatService> fieldFormateMap) {
        this.fieldFormateMap = fieldFormateMap;
    }

    public void setNeedUpdateFields(Map<String, List<String>> needUpdateFields) {
        this.needUpdateFields = needUpdateFields;
    }

    public void setSlaveTableAddedForUpdateWide(Map<String, List<String>> slaveTableAddedForUpdateWide) {
        this.slaveTableAddedForUpdateWide = slaveTableAddedForUpdateWide;
    }

    public void setTableNeedField(Map<String, List<String>> tableNeedField) {
        this.tableNeedField = tableNeedField;
    }

    public void setWideIndexLinkSubTableField(Map<String, Map<String, Collection<String>>> wideIndexLinkSubTableField) {
        this.wideIndexLinkSubTableField = wideIndexLinkSubTableField;
    }

    public void setSpecialFieldMap(Map<String, SpecialFieldService> specialFieldMap) {
        this.specialFieldMap = specialFieldMap;
    }

    public void setDynamicIndexdMap(Map<String, DynmaicIndexService> dynamicIndexdMap) {
        this.dynamicIndexdMap = dynamicIndexdMap;
    }

    public void setFixFieldMap(Map<String, FixFieldService> fixFieldMap) {
        this.fixFieldMap = fixFieldMap;
    }

    public void setFieldTypeMap(Map<String, FieldTypeService> fieldTypeMap) {
        this.fieldTypeMap = fieldTypeMap;
    }

    public static void main(String[] args) {
    }
}
