package com.alibaba.otter.common.push.index.wide.config;

import com.alibaba.otter.shared.common.utils.NameFormatUtils;

public class CurriculumWideIndexConstants {
    public static final String CURRICULUM_WIDE_TABLE_OR_INDEX_NAME = "curriculum";
    public static final String CURRICULUM_CHANGE_OUT_COURSE_NUM = CURRICULUM_WIDE_TABLE_OR_INDEX_NAME + "_changeoutCourseNum";
    public static final String CURRICULUM_CHANGE_IN_COURSE_NUM = CURRICULUM_WIDE_TABLE_OR_INDEX_NAME + "_changeinCourseNum";
    public static final String CURRICULUM_WIDE_INDEX_PKID_NAME = CURRICULUM_WIDE_TABLE_OR_INDEX_NAME + "_id";
    public static final String CURRICULUM_REMAIN_COUNT = "remainCount";
    public static final String CURRICULUM_ORDER_REMAIN_COUNT = "preRemainCount";

    public static final String CLAZZ_TABLE_OR_INDEX_NAME = "clazz";
    public static final String CLAZZ_MAX_PERSONS = CLAZZ_TABLE_OR_INDEX_NAME + "_maxPersons";
    public static final String CLAZZ_INDEX_PKID_NAME = CLAZZ_TABLE_OR_INDEX_NAME + "_id";
    public static final String CLAZZ_INDEX_SUBJECT_LONG_VALUE_NAME = CLAZZ_TABLE_OR_INDEX_NAME + "_subjectLongValue";



    public static final String TEACHER_TABLE_OR_INDEX_NAME = "teacher";
    public static final String TEACHER_INDEX_PKID_NAME = TEACHER_TABLE_OR_INDEX_NAME + "_id";

    public static final String SYSTEM_PARAM_TABLE_OR_INDEX_NAME = "system_param";
    public static final String SYSTEM_PARAM_INDEX_PKID_NAME = NameFormatUtils.formatName(SYSTEM_PARAM_TABLE_OR_INDEX_NAME) + "_id";

    public static final String DISTRICT_TABLE_OR_INDEX_NAME = "district";
    public static final String DISTRICT_INDEX_PKID_NAME = DISTRICT_TABLE_OR_INDEX_NAME + "_id";

    public static final String DEPARTMENT_TABLE_OR_INDEX_NAME = "department";
    public static final String DEPARTMENT_INDEX_PKID_NAME = DEPARTMENT_TABLE_OR_INDEX_NAME + "_id";

    public static final String CLASSTIME_TYPE_TABLE_OR_INDEX_NAME = "classtime_type";
    public static final String CLASSTIME_TYPE_INDEX_PKID_NAME = NameFormatUtils.formatName(CLASSTIME_TYPE_TABLE_OR_INDEX_NAME) + "_id";

    public static final String CLASSTIME_TABLE_OR_INDEX_NAME = "classtime";
    public static final String CLASSTIME_INDEX_PKID_NAME = CLASSTIME_TABLE_OR_INDEX_NAME + "_id";

    public static final String CLASSLEVEL_TABLE_OR_INDEX_NAME = "classlevel";
    public static final String CLASSLEVEL_INDEX_PKID_NAME = CLASSLEVEL_TABLE_OR_INDEX_NAME + "_id";

    public static final String CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME = "class_regist_count";
    public static final String CLASS_REGIST_COUNT_INDEX_PKID_NAME = NameFormatUtils.formatName(CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME) + "_id";
    public static final String CLASS_REGIST_COUNT_REGIST_COUNT = NameFormatUtils.formatName(CLASS_REGIST_COUNT_TABLE_OR_INDEX_NAME) +"_registCount";

    public static final String CHANGE_COURSE_AMOUNT_TABLE_OR_INDEX_NAME = "change_course_amount";
    public static final String CHANGE_COURSE_AMOUNT_INDEX_PKID_NAME = NameFormatUtils.formatName(CHANGE_COURSE_AMOUNT_TABLE_OR_INDEX_NAME) + "_id";

    public static void main(String[] args) {
        System.out.println(CHANGE_COURSE_AMOUNT_INDEX_PKID_NAME);
    }

}