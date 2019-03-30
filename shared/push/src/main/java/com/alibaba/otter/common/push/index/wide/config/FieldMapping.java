package com.alibaba.otter.common.push.index.wide.config;

import com.alibaba.otter.shared.etl.model.EventColumn;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FieldMapping {

    public static final String NULL_VALUE = "null";
    public static final String WIDE_TABLE_SYNC_FIELD_ES_STATUS = "esStatus";

    /**
     * =调课转班=>
     */
    public static final Map<String, String> CURRICULUM_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("cuc_id", "id");
        put("cuc_class_id", "class_id");
        put("cuc_classtimeType_id", "classtime_type_id");
        put("cuc_classtime_id", "classtime_id");
        put("cuc_class_num", "class_num");
        put("cuc_status", "status");
        put("cuc_deleted", "is_delete");
        put("cuc_changeout_course_num", "changeout_course_num");
        put("cuc_changein_course_num", "changein_course_num");
        put("cuc_teacher_id", "teacher_id");
        put("cuc_subject_id", "subject_id");
        put("cuc_tutor_id", "tutor_id");
        put("cuc_tutor_real_name", "tutor_real_name");
        put("cuc_teacher_type", "teacher_type");
        put("cuc_classroom_id", "classroom_id");
        put("cuc_lock_status", "lock_status");
        put("cuc_cancel", "cancel");
        put("cuc_name", "name");
        put("cuc_modify_time", "modify_time");
        put("tcc_is_attend", "is_attend");//是否充许旁听
        put("tcc_source_class_id", "source_class_id");//续报原班ID


    }};


    public static final Map<String, String> CLASS_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("cla_id", "id");
        put("cla_name", "name");
        put("cla_area_name", "area_name");
        put("cla_classroom_name", "classroom_name");
        put("cla_classdate_name", "classdate_name");
        put("cla_year", "year");
        put("cla_is_live_class", "is_live_class");
        put("cla_tutor_id", "tutor_id");
        put("cla_tutor_real_name", "tutor_real_name");
        put("cla_classtime_timePeriod", "classtime_time_period");
        put("cla_classtime_ids", "classtime_ids");//上课时间ID(多个)
        put("cla_classtime_names", "classtime_names");//上课时间名称
        put("cla_class_type", "class_type");
        put("cla_venue_id", "venue_id");
        put("cla_venue_name", "venue_name");
        put("cla_area_id", "area_id");
        put("cla_teacher_ids", "teacher_ids");
        put("cla_teacher_names", "teacher_names");
        put("cla_is_double_teacher_live_class", "is_double_teacher_live_class");
        put("cla_start_date", "start_date");
        put("cla_end_date", "end_date");
        put("cla_term_id", "term_id");
        put("cla_term_name", "term_name");//学期名称
        put("cla_gt_name", "grade_type_name");//年部名称 FIXME
        put("cla_gt_id", "grade_type_id");//年部名称 FIXME
        put("cla_grade_id", "grade_id");//FIXME
        put("cla_grade_name", "grade_name");//年级名称
        put("cla_level_id", "level_id");
        put("cla_level_name", "level_name");//班次名称
        put("cla_servicecenter_id", "servicecenter_id");
        put("cla_servicecenter_name", "servicecenter_name");//服务中心名称
        put("cla_servicecenter_address", "servicecenter_address");//服务中心地址
        put("cla_subject_ids", "subject_long_value");
        put("cla_subject_names", "subject_names");//学科名称
        put("cla_class_count", "class_count");
        put("cla_price", "price");
        put("cla_passed_count", "passed_count");
        put("cla_is_hidden", "is_hidden");
        put("cla_is_test", "is_test");
        put("cla_deleted", "is_delete");
        put("cla_is_close", "is_close");
        put("cla_max_persons", "max_persons");
        put("cla_biz_type", "biz_type");
        put("cla_create_type", "create_type");
        put("cla_modify_date", "modify_date");
        put("cla_exam_id", "exam_id");
        put("cla_template_id", "template_id");//班级模板ID
        put("cla_classdate_id", "classdate_id");//上课日期ID
        put("cla_isdisplay_front", "is_display_front");//前台是否显示(0：否；1：是；)
        put("cla_isdisplay_teacher", "is_display_teacher");//教师系统是否显示(0：否；1：是；)
        put("cla_courseware_send", "courseware_send");//环迅教育排课是否完成(0:否,1:是)
        put("cla_course_id", "course_id");//环迅教育排课是否完成(0:否,1:是)
        put("cla_nickName", "nickName");//环迅教育排课是否完成(0:否,1:是)
        put("cla_source_class_id", "source_class_id");//环迅教育排课是否完成(0:否,1:是)
        put("cla_is_schooltest", "is_schooltest");//环迅教育排课是否完成(0:否,1:是)
        put("cla_classTime_Type_Id", "classTime_Type_Id");//环迅教育排课是否完成(0:否,1:是)
        put("cla_class_change_name", "class_change_name");//环迅教育排课是否完成(0:否,1:是)


    }};


    //FIXME TEST key is match ?
    public static final Map<String, String> CONVERTER_FIELD = new ConcurrentHashMap() {{
        put("clazz_classtimeNames", NULL_VALUE);
        put("clazz_classtimeIds", NULL_VALUE);
        put("clazz_teacherIds", NULL_VALUE);
        put("clazz_teacherNames", NULL_VALUE);
        put("clazz_classtimeNames", NULL_VALUE);
        put("clazz_classTimeTypeId", NULL_VALUE);
    }};

    /**
     * 转换字段类型
     *
     * @param key
     * @param column
     * @param jsonMap
     */
    public static void convertValues(String key, EventColumn column, Map<String, Object> jsonMap) {
        if (null != FieldMapping.CONVERTER_FIELD.get(key) && !column.isNull()) {
            jsonMap.put(key, column.getColumnValue().split("[|,]"));
        }
    }


    public static final Map<String, List<Object[]>> ADD_TABLE_FIXE_VALUE_COLUMS = new ConcurrentHashMap() {{
        put("curriculum", new ArrayList<Object[]>() {{
                    add(new Object[]{WIDE_TABLE_SYNC_FIELD_ES_STATUS, FieldHelper.ES_SYNC_WIDE_INDEX_INIT});
                }}
        );
    }};

    /**
     * 给index添加额外的字段；
     *
     * @param tableName
     * @param jsonMap
     */
    public static void addColumns(String tableName, Map<String, Object> jsonMap) {
        List<Object[]> list = ADD_TABLE_FIXE_VALUE_COLUMS.get(tableName);
        if (null != list) {
            list.forEach(objects -> {
                jsonMap.put(objects[0].toString(), objects[1]);
            });
        }
    }

    public static final Map<String, String> DEPARTMENT_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("dept_id", "id");
        put("dept_name", "name");
        put("dept_district_id", "district_id");

    }};

    public static final Map<String, String> REGISTER_COUNT_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("crc_id", "id");
        put("crc_class_id", "class_id");
        put("crc_regist_count", "regist_count");
        put("crc_modify_date", "modify_date");

    }};

    public static final Map<String, String> CLASS_LEVEL_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("lev_id", "id");
        put("lev_degree", "degree");
        put("lev_status", "status");
        put("term_is_show_student", "term_is_show_student");
        put("lev_degree", "degree");
        put("lev_name", "name");

    }};

    public static final Map<String, String> CLASS_TIME_TYPE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("ctt_id", "id");
        put("ctt_timeType_classify", "timeType_classify");
        put("ctt_name", "name");
        put("ctt_timeType_classifyInt", "timeType_classifyInt");

    }};

    public static final Map<String, String> CLASS_TIME_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("ct_id", "id");
        put("ct_period_type", "period_type");
        put("ct_period", "period");
        put("ct_time_name", "time_name");

    }};


    /**
     * <=宽表=>
     */

    /**
     * 全表同步
     */

    public static final Map<String, String> DISTRICT_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("d_id", "id");
        put("d_name", "name");
        put("d_description", "description");
        put("d_link_url", "link_url");
        put("d_order", "order");
        put("d_code", "code");
        put("d_city_id", "city_id");
        put("d_deleted", "deleted");
        put("d_creater_id", "creater_id");
        put("d_create_date", "create_date");

    }};


    /**
     * FIXME 单表 teacher tutor tb_regist  tb_system_param  tb_change_course_amount
     */
    public static final Map<String, String> TEACHER_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("tea_id", "id");
        put("tea_sex", "sex");
        put("city_id", "city_id");
        put("tea_teacher_name", "teacher_name");
        put("tea_picture_url", "picture_url");
        put("tea_teacher_code", "teacher_code");

    }};

    public static final Map<String, String> TUTOR_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("tutor_id", "id");
        put("city_id", "city_id");
        put("tutor_sex", "sex");
        put("tutor_RealName", "RealName");
        put("tutor_sysName", "sysName");
        put("tutor_imgUrl", "imgUrl");

    }};

    public static final Map<String, String> SYSTEM_PARAM_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("sp_id", "id");
        put("city_id", "city_id");
        put("sp_param_name", "param_name");
        put("sp_param_value", "param_value");
        put("sp_param_alias", "param_alias");
        put("sp_deleted", "deleted");
        put("sp_create_date", "create_date");
        put("sp_version", "version");

    }};

    public static final Map<String, String> CHANGE_COURSE_AMOUNT_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("cca_id", "id");
        put("city_id", "city_id");
        put("cca_class_type", "class_type");
        put("cca_grade_type_id", "grade_type_id");
        put("cca_grade_type_name", "grade_type_name");
        put("cca_grade_id", "grade_id");
        put("cca_grade_name", "grade_name");
        put("cca_change_course_amount", "change_course_amount");
        put("cca_isdeleted", "isdeleted");
        put("cca_creater_id", "creater_id");
        put("cca_creater_name", "creater_name");
        put("cca_create_date", "create_date");
        put("cca_modifier_id", "modifier_id");
        put("cca_modifier_name", "modifier_name");
        put("cca_modify_date", "modify_date");
    }};

    public static final Map<String, String> REGISTER_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("reg_id", "id");
        put("city_id", "city_id");
        put("reg_class_id", "class_id");
        put("reg_iscontinue", "iscontinue");
        put("reg_isdeleted", "isdeleted");
        put("reg_pay_date", "pay_date");
        put("reg_student_id", "student_id");
        put("reg_ispay", "ispay");
        put("reg_way", "way");
        put("reg_class_no", "class_no");
        put("reg_add_class_quantity", "add_class_quantity");
        put("reg_pay_enddate", "pay_enddate");
        put("reg_source_class_id", "source_class_id");
        put("reg_is_test", "is_test");
//        put("reg_is_statistic", "is_statistic");
//        put("reg_listener_code", "listener_code");
//        put("reg_listenercard_id", "listenercard_id");
//        put("reg_create_date", "create_date");
//        put("reg_create_id", "create_id");
//        put("reg_remark", "remark");
//        put("reg_version", "version");
//        put("reg_is_newstu", "is_newstu");
//        put("reg_passedcount", "passedcount");
//        put("reg_isbankpay", "isbankpay");
//        put("reg_reserved_times", "reserved_times");
//        put("reg_is_reserved", "is_reserved");
//        put("reg_reserve_source_class_id", "reserve_source_class_id");
//        put("reg_reserved_change_classes_deadline", "reserved_change_classes_deadline");
//        put("reg_is_receipt_books", "is_receipt_books");
//        put("reg_modify_id", "modify_id");
//        put("reg_modify_date", "modify_date");
//        put("reg_bi_test", "bi_test");
//        put("reg_candidate_no", "candidate_no");
//        put("reg_candidate_serviceCenter_id", "candidate_serviceCenter_id");
//        put("reg_seat", "seat");
//        put("reg_is_send_sms", "is_send_sms");
//        put("reg_beili_course_id", "beili_course_id");
//        put("reg_is_beili_course_no", "is_beili_course_no");
//        put("reg_crm_statistic", "crm_statistic");
//        put("reg_activity", "activity");
//        put("reg_associated_common_class_type", "associated_common_class_type");
//        put("reg_associated_common_class_reg_id", "associated_common_class_id");

    }};


    /** <=调课转班=> */


//年+学期->学部 年+学期+学部->年级 年+学期+学部+年级+学科->班次
//cla_gt_id cla_gt_name cla_deleted  cla_year  cla_term_id  cla_is_live_class  cla_passed_count  cla_gt_id cla_class_count
//cla_level_id cla_level_name  cla_is_hidden cla_year    cla_term_id cla_grade_id cla_subject_ids cla_deleted cla_gt_id  cla_is_live_class cla_passed_count cla_class_count
//cla_grade_id cla_grade_name cla_gt_id cla_gt_name cla_deleted  cla_year  cla_term_id  cla_is_live_class  cla_passed_count  cla_gt_id cla_class_count

    /**
     * 反推接口
     */
    public static final Map<String, String> CLASS_SINGLE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("cla_id", "id");
        put("cla_name", "name");
        put("cla_gt_id", "grade_type_id");//年部名称
        put("cla_gt_name", "grade_type_name");//年部名称
        put("cla_deleted", "is_delete");
        put("cla_year", "year");
        put("cla_term_id", "term_id");
        put("cla_grade_id", "grade_id");
        put("cla_grade_name", "grade_name");//年级名称
        put("cla_is_live_class", "is_live_class");
        put("cla_passed_count", "passed_count");
        put("cla_subject_ids", "subject_long_value");
        put("cla_class_count", "class_count");
        put("cla_level_id", "level_id");
        put("cla_level_name", "level_name");//班次名称
        put("cla_is_hidden", "is_hidden");
        put("cla_subject_ids", "subject_ids");

    }};


    public static final Map<String, String> CLASS_LEVEL_SINGLE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("lev_id", "id");
        put("lev_name", "name");
        put("lev_city_id", "city_id");
        put("lev_order", "order");
        put("lev_display_name", "display_name");
        put("lev_degree", "degree");
        put("lev_parent_id", "parent_id");
        put("lev_type", "type");
        put("lev_deleted", "deleted");
        put("lev_create_date", "create_date");
        put("lev_creater_id", "creater_id");
        put("lev_modify_date", "modify_date");
        put("lev_modify_id", "modify_id");
        put("lev_status", "status");
        put("lev_is_leaf", "is_leaf");
        put("lev_term_is_show_student", "term_is_show_student");
        put("lev_term_is_show_teacher", "term_is_show_teacher");
        put("lev_is_mobi", "is_mobi");
        put("lev_is_live_lesson", "is_live_lesson");
        put("lev_disable", "disable");


    }};

    public static final Map<String, String> GRADE_TYPE_SINGLE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("gt_id", "id");
        put("gt_name", "name");
        put("gt_order", "order");
        put("gt_create_date", "create_date");
        put("gt_isdeleted", "isdeleted");
        put("gt_modify_date", "modify_date");
        put("gt_status", "status");
        put("gt_creater_id", "creater_id");

    }};

    public static final Map<String, String> GRADE_SINGLE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("grd_id", "id");
        put("grd_name", "name");
        put("grd_city_id", "city_id");
        put("grd_order", "order");
        put("grd_digits", "digits");
        put("grd_type_id", "type_id");
        put("grd_isdeleted", "isdeleted");
        put("grd_status", "status");
        put("grd_create_date", "create_date");
        put("grd_creater_id", "creater_id");
        put("grd_modify_id", "modify_id");
        put("grd_modify_date", "modify_date");
        put("grd_fullclass_showstatus", "fullclass_showstatus");

    }};

    public static final Map<String, String> SUBJECT_SINGLE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("subj_id", "id");
        put("subj_name", "name");
        put("subj_city_id", "city_id");
        put("subj_order", "order");
        put("subj_type", "type");
        put("subj_is_show_tea", "is_show_tea");
        put("subj_is_show_stu", "is_show_stu");
        put("subj_isdeleted", "isdeleted");
        put("subj_status", "status");
        put("subj_create_date", "create_date");
        put("subj_creater_id", "creater_id");
        put("subj_modify_id", "modify_id");
        put("subj_modify_date", "modify_date");
        put("subj_allow_samesubject", "allow_samesubject");
        put("subj_is_show_ser", "is_show_ser");
        put("subj_long_value", "long_value");
        put("subj_is_live_lesson", "is_live_lesson");

    }};

    /**
     *
     */
    public static final Map<String, String> REGIST_WIDE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("reg_id", "id");
        put("city_id", "city_id");
        put("reg_class_id", "class_id");
        put("reg_isdeleted", "isdeleted");
        put("reg_pay_date", "pay_date");
        put("reg_student_id", "student_id");

    }};


    public static final Map<String, String> REGIST_CLASS_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("subj_id", "id");
        put("subj_name", "name");
        put("subj_city_id", "city_id");
        put("cla_id", "id");
        put("cla_term_id", "term_id");
        put("cla_deleted", "deleted");
        put("cla_is_test", "is_test");
        put("cla_is_close", "is_close");
        put("cla_passed_count", "passed_count");
        put("cla_count", "class_count");
        put("cla_year", "year");
        put("cla_name", "name");
        put("cla_area_name", "area_name");
        put("cla_servicecenter_name", "servicecenter_name");
        put("cla_venue_id", "venue_id");
        put("cla_oom_name", "classroom_name");
        put("cla_ime_ids", "classtime_ids");
        put("cla_start_date", "start_date");
        put("cla_end_date", "end_date");
        put("cla_ime_names", "classtime_names");
        put("cla_teacher_ids", "teacher_ids");
        put("cla_teacher_names", "teacher_names");
        put("cla_level_id", "level_id");
        put("cla_level_name", "level_name");
        put("cla_grade_id", "grade_id");
        put("cla_subject_ids", "subject_ids");
        put("cla_is_live_class", "is_live_class");
        put("cla_ate_name", "classdate_name");
        put("cla_servicecenter_id", "servicecenter_id");
    }};


    //FIXME regist stage wide index start
    public static final Map<String, String> RGSE_REGISTER_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("reg_id", "id");
        put("city_id", "city_id");
        put("reg_create_id", "create_id");
        put("reg_create_date", "create_date");
        put("reg_pay_enddate", "pay_enddate");
        put("reg_way", "way");
        put("reg_is_newstu", "is_newstu");
        put("reg_remark", "remark");
        put("reg_is_reserved", "is_reserved");

        //ext
        put("reg_pay_date", "pay_date");
        put("reg_class_id", "class_id");
        put("reg_isdeleted", "isdeleted");
        put("reg_student_id", "student_id");
        put("reg_ispay", "ispay");
        put("reg_is_test", "is_test");
        put("reg_source_class_id", "source_class_id");
        put("reg_modify_date", "modify_date");

    }};

    public static final Map<String, String> RGSE_STUDENT_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("city_id", "city_id");
        put("stu_uid", "uid");
        put("stu_id", "id");
        put("stu_loginname", "loginname");
        put("stu_student_code", "student_code");
        put("stu_name", "name");
        put("stu_if_paidregfee", "if_paidregfee");

        //ext
        put("stu_modify_date", "modify_date");
    }};


    public static final Map<String, String> RGSE_REGIST_STAGE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("city_id", "city_id");
        put("id", "id");
        put("regist_id", "regist_id");
        put("class_id", "class_id");
        put("class_stage_id", "class_stage_id");
        put("student_id", "student_id");
        put("curriculum_count", "curriculum_count");
        put("payed", "payed");

        //ext
        put("add_curriculum_count", "add_curriculum_count");
        put("deleted", "deleted");
        put("auto_cancel", "auto_cancel");
        put("pay_time", "pay_time");
        put("modify_time", "modify_time");
        put("tm_time", "tm_time");
    }};


    public static final Map<String, String> RGSE_CLASS_STAGE_INCLUDE_FIELD = new ConcurrentHashMap() {{
        put("city_id", "city_id");
        put("id", "id");
        put("stages_name", "stages_name");
        put("stages_num", "stages_num");
        put("update_time", "update_time");

        //ext
    }};

    //FIXME regist stage wide index end


    public static final Map<String, Map<String, String>> INDEX_FIELD_MAPPING = new ConcurrentHashMap() {
        {
            put("curriculum", CURRICULUM_INCLUDE_FIELD);
            put("clazz", CLASS_INCLUDE_FIELD);
            put("department", DEPARTMENT_INCLUDE_FIELD);
            put("class_regist_count", REGISTER_COUNT_INCLUDE_FIELD);
            put("classlevel", CLASS_LEVEL_INCLUDE_FIELD);
            put("classtime_type", CLASS_TIME_TYPE_INCLUDE_FIELD);
            put("classtime", CLASS_TIME_INCLUDE_FIELD);
            put("district", DISTRICT_INCLUDE_FIELD);
            put("teacher", TEACHER_INCLUDE_FIELD);
            put("tutor", TUTOR_INCLUDE_FIELD);
            put("system_param", SYSTEM_PARAM_INCLUDE_FIELD);
            put("rgse", RGSE_REGIST_STAGE_INCLUDE_FIELD);
            put("stu", RGSE_STUDENT_INCLUDE_FIELD);
            put("rg", RGSE_REGISTER_INCLUDE_FIELD);
            put(RegistStageWideIndexConstants.CLASS_STAGE_TABLE_OR_INDEX_NAME, RGSE_CLASS_STAGE_INCLUDE_FIELD);
        }
    };

    public static final Map<String, String> TABLE_SOURCE_ID_MAP = new ConcurrentHashMap() {
        {
            put("curriculum", "cuc_id");
            put("clazz", "cla_id");
            put("department", "dept_id");
            put("class_regist_count", "reg_id");
            put("classlevel", "lev_id");
            put("classtime_type", "");
            put("classtime", "ct_id");
            put("district", "d_id");
            put("system_param", "sp_id");
        }
    };


    public static final Set<String> NO_CITY_INDEX = new HashSet<String>() {
        {
            add("district");
        }
    };


    public static void main(String[] args) {
        try {
            List<String> list = FileUtils.readLines(new File("/Users/zscome/a.log"));
            System.out.println(list.size());
            String collect = list.stream().collect(Collectors.joining(","));
            List<String> list1 = Arrays.asList(collect.split(","));
            System.out.println("new = "+list1.size());
            System.out.println();
            System.out.println(collect);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
