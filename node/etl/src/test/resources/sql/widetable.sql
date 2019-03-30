select COLUMN_NAME  from information_schema.COLUMNS where table_name = 'tb_curriculum' and table_schema = 'otter';

-- tb_class
select  cla_id,cla_name,cla_gt_id,cla_gt_name,cla_year,cla_term_name,cla_term_id,cla_atte_stat_type_id,cla_atte_stat_type_name,cla_grade_id,cla_grade_name,cla_level_id,cla_level_name,cla_classroom_id,cla_classroom_name,cla_servicecenter_id,cla_servicecenter_name,cla_venue_id,cla_venue_name,cla_area_id,cla_area_name,cla_template_id,cla_classdate_id,cla_classdate_name,cla_subject_ids,cla_subject_names,cla_classtime_ids,cla_classtime_names,cla_class_change_id,cla_class_change_name,cla_teacher_ids,cla_teacher_names,cla_start_date,cla_end_date,cla_class_count,cla_passed_count,cla_frequency,cla_exclude_date,cla_every_hours,cla_price,cla_max_persons,cla_alternate_persons,cla_haspersons,cla_is_schooltest,cla_is_attend,cla_feetype,cla_source_class,cla_pay_end_date,cla_version,cla_is_test,cla_is_statistic,cla_status,cla_isdisplay_front,cla_isdisplay_teacher,cla_isdisplay_student,cla_deleted,cla_is_close,cla_is_expired,cla_is_recommend,cla_is_hidden,cla_create_date,cla_creater_id,cla_modify_date,cla_modify_id,cla_is_pay,cla_teacher_codes,cla_add_max_persons,cla_add_max_start,cla_add_max_end,cla_class_teacher_id,cla_class_teacher_name,cla_audit_status,cla_auditor_id,cla_audit_date,cla_curr_lock_status,cla_lock_log_id,cla_is_mobi,cla_bi_test,cla_is_fee,cla_is_collection_charges,cla_exam_id,cla_servicecenter_address,cla_classTime_Type_Id,cla_classtime_timePeriod,cla_classtime_timeClassify,cla_is_beili_build,cla_is_beili,cla_is_web_regist,cla_is_push_to_xes_im,is_push_to_xes_im,cla_tutor_id,cla_tutor_real_name,class_introduction,class_introduction_modifier,class_introduction_modified_time,cla_xes_order,cla_xes_surplus,cla_live_number_id,cla_live_class_template_id,cla_live_class_time_id,cla_is_live_class,cla_live_class_type,cla_is_double_teacher_live_class,cla_recommend_number,cla_recommend_number_MD5,cla_serial_no,cla_class_type,cla_nickname,cla_admin_status,cla_course_id,cla_teacher_conflict,cla_room_conflict,cla_courseware_send,cla_headquarters_openclass,cla_biz_type,cla_create_type
from beijing_xxgl.tb_class limit 1 ;






-- tb_department

select dept_id,dept_discriminator,dept_name,dept_parent_id,dept_is_serv,dept_create_date,dept_master,dept_master_tel,dept_second_person,dept_second_person_tel,dept_phone,dept_address,dept_areaid,dept_order,dept_serv_order,dept_terminal_no,dept_code,dept_city_id,dept_is_show_student,dept_is_show_teacher,dept_creater_id,dept_is_receive,dept_deleted,dept_disable,dept_longitude,dept_latitude,dept_serv_address,dept_is_call_service_center,dept_first_char,dept_district_id,dept_mobile_alias,dept_mobile_area_id,dept_is_show_in_mobile,dept_business_reimbursement_date,dept_simple_name
from beijing_xxgl.tb_department limit 1;

select * from beijing_xxgl.tb_classtime_type limit 1 ;

-- tb_class_regist_count
select * from beijing_xxgl.tb_class_regist_count limit 1;

select * from beijing_xxgl.tb_classtime limit 1;

-- tb_curriculum
select cuc_id,cuc_class_id,cuc_class_num,cuc_class_date,cuc_classtime_id,cuc_start_time,cuc_end_time,cuc_teacher_id,cuc_subject_id,cuc_teacher_type,cuc_classroom_id,cuc_deleted,cuc_status,cuc_attendance,cuc_changein_course_num,cuc_changeout_course_num,cuc_version,cuc_lock_status,cuc_cancel,cuc_print_date,cuc_bi_test,cuc_classtimeType_id,cuc_classTime_name,cuc_name,cuc_tutor_id,cuc_tutor_real_name,cuc_teacher_conflict,cuc_room_conflict,cuc_modify_time
from beijing_xxgl.tb_curriculum limit 1;

select * from beijing_xxgl.tb_classlevel limit 1 ;

truncate table otter.tb_class ;
truncate table otter.tb_department ;
truncate table otter.tb_class_regist_count ;
truncate table otter.tb_classtime_type ;
truncate table otter.tb_classtime ;
truncate table otter.tb_classlevel ;
truncate table otter.tb_curriculum ;

-- cuc_classtime_id =time_1 ;
-- class_id=1;
-- classRegistCount_classId=1;
-- cla_servicecenter_id=dep_1;
-- classtimeType_id= timetype_1

-- 有关联的数据

truncate table otter.tb_class ;
truncate table otter.tb_department ;
truncate table otter.tb_class_regist_count ;
truncate table otter.tb_classtime_type ;
truncate table otter.tb_classtime ;
truncate table otter.tb_classlevel ;
truncate table otter.tb_curriculum ;

insert into `otter`.`tb_classlevel` ( `lev_id`, `lev_parent_id`, `lev_create_date`, `lev_status`, `lev_name`, `lev_is_mobi`, `is_live_lesson`, `lev_disable`, `lev_modify_id`, `lev_order`, `lev_modify_date`, `term_is_show_teacher`, `lev_display_name`, `lev_degree`, `term_is_show_student`, `lev_type`, `lev_creater_id`, `lev_deleted`, `lev_is_leaf`) values ( 'level_1', '0', '2014-10-02 16:36:08', '0', '北京移动端测试G班(测试勿报名)', '0', '0', '1', '52b38a251d6c3d15011d71d113c30441', '1111', '2014-10-02 16:36:08', '0', null, '1.00', '1', '1', '52b38a251d6c3d15011d71d113c30441', '0', '1');
insert into `otter`.`tb_department` ( `dept_mobile_alias`, `dept_is_show_student`, `dept_id`, `dept_master`, `dept_is_call_service_center`, `dept_is_receive`, `dept_create_date`, `dept_latitude`, `dept_is_show_teacher`, `dept_serv_order`, `dept_master_tel`, `dept_areaid`, `dept_city_id`, `dept_deleted`, `dept_name`, `dept_business_reimbursement_date`, `dept_parent_id`, `dept_mobile_area_id`, `dept_second_person`, `dept_disable`, `dept_first_char`, `dept_district_id`, `dept_serv_address`, `dept_longitude`, `dept_second_person_tel`, `dept_discriminator`, `dept_creater_id`, `dept_is_serv`, `dept_code`, `dept_simple_name`, `dept_address`, `dept_is_show_in_mobile`, `dept_phone`, `dept_order`, `dept_terminal_no`) values ( null, '1', 'dep_1', 'ff80808138716e7e01388dce557f32d7', '1', '0', '2014-10-30 08:52:07', '39.989495', '1', null, '111', '52b38a2c22cf94dd0122cfb0f5b600f6', '110100', '0', '惠新西街', '2018-08-11 00:00:00', 'ff8080812819ee6501281a58338500ac', null, 'ff80808138716e7e01388dce557f32d7', '0', 'HXXJ', '110105', '北京市朝阳区干杨树街甲16号中润珠宝城三层123', '116.418132', '111', 'servicecenter', '52b38a251d6c3d15011d71d113c30441', '1', '181', '惠新西街', '北京市朝阳区干杨树街甲16号中润珠宝城三层123', '0', '12121213131', '614048046120', '98561235');
insert into `otter`.`tb_class_regist_count` ( `crc_regist_count`, `crc_id`, `crc_modify_date`, `crc_id_version`, `crc_class_id`) values ( '13', '1', '2014-11-16 17:53:25', '27', '1');
insert into `otter`.`tb_classtime_type` ( `ctt_id`, `ctt_create_id`, `ctt_order`, `ctt_deleted`, `ctt_name`, `ctt_modify_id`, `ctt_create_date`, `ctt_modify_date`, `ctt_timeType_classifyInt`, `ctt_timeType_classify`, `ctt_className_use`) values ( 'timetype_1', null, '11', '0', '2.1-7', null, '2014-11-01 11:24:29', '2015-01-21 14:14:48', '1,2,3,4,5,6,7', '周一,周二,周三,周四,周五,周六,周日', '1');
insert into `otter`.`tb_classtime` ( `ct_is_show_teacher`, `ct_time_name`, `ct_period`, `ct_type_id`, `ct_status`, `ct_modify_date`, `ct_start_time`, `ct_period_type`, `ct_order`, `ct_class_hours`, `ct_id`, `ct_create_date`, `ct_deleted`, `ct_end_time`, `ct_create_id`, `ct_modify_id`) values ( '0', '上午08:00-11:55', '上午', null, '0', '2016-07-15 10:02:25', '08:00:00', '1', null, '3.9', 'time_1', '2014-11-01 10:06:38', '0', '11:55:00', '52b38a251d6c3d15011d71d113c30441', '402887f22567bef9012567cc7e400001');
insert into `otter`.`tb_class` ( `cla_servicecenter_address`, `cla_create_type`, `cla_tutor_id`, `cla_class_change_name`, `cla_version`, `cla_gt_name`, `class_introduction`, `class_introduction_modifier`, `cla_add_max_persons`, `cla_start_date`, `cla_classroom_name`, `cla_term_id`, `cla_subject_ids`, `cla_frequency`, `cla_end_date`, `cla_class_change_id`, `cla_exam_id`, `cla_atte_stat_type_id`, `cla_exclude_date`, `cla_is_expired`, `cla_servicecenter_id`, `cla_source_class`, `cla_classdate_id`, `cla_is_test`, `cla_is_beili_build`, `cla_classtime_ids`, `cla_classdate_name`, `cla_tutor_real_name`, `cla_teacher_ids`, `cla_price`, `cla_name`, `cla_status`, `cla_teacher_names`, `cla_live_class_template_id`, `cla_is_live_class`, `cla_admin_status`, `cla_recommend_number`, `cla_feetype`, `cla_live_class_time_id`, `cla_nickname`, `cla_teacher_conflict`, `cla_subject_names`, `cla_haspersons`, `cla_audit_date`, `cla_lock_log_id`, `cla_venue_id`, `cla_auditor_id`, `cla_serial_no`, `cla_modify_id`, `cla_isdisplay_front`, `cla_class_count`, `cla_level_id`, `cla_is_close`, `cla_xes_order`, `cla_create_date`, `cla_grade_id`, `is_push_to_xes_im`, `cla_passed_count`, `cla_is_mobi`, `cla_add_max_start`, `cla_is_statistic`, `cla_courseware_send`, `class_introduction_modified_time`, `cla_recommend_number_MD5`, `cla_deleted`, `cla_template_id`, `cla_id`, `cla_servicecenter_name`, `cla_grade_name`, `cla_gt_id`, `cla_creater_id`, `cla_is_collection_charges`, `cla_headquarters_openclass`, `cla_is_schooltest`, `cla_alternate_persons`, `cla_is_recommend`, `cla_isdisplay_teacher`, `cla_curr_lock_status`, `cla_audit_status`, `cla_pay_end_date`, `cla_is_beili`, `cla_venue_name`, `cla_live_class_type`, `cla_year`, `cla_is_double_teacher_live_class`, `cla_class_teacher_name`, `cla_classtime_timeClassify`, `cla_term_name`, `cla_is_pay`, `cla_class_teacher_id`, `cla_teacher_codes`, `cla_is_fee`, `cla_every_hours`, `cla_add_max_end`, `cla_is_attend`, `cla_classroom_id`, `cla_classtime_names`, `cla_bi_test`, `cla_modify_date`, `cla_is_push_to_xes_im`, `cla_course_id`, `cla_biz_type`, `cla_class_type`, `cla_xes_surplus`, `cla_area_name`, `cla_isdisplay_student`, `cla_atte_stat_type_name`, `cla_classtime_timePeriod`, `cla_classTime_Type_Id`, `cla_area_id`, `cla_room_conflict`, `cla_max_persons`, `cla_is_web_regist`, `cla_level_name`, `cla_live_number_id`, `cla_is_hidden`) values ( null, '0', null, null, null, '小学部', null, null, '3', '2014-11-18', '奥亚酒店403', '5', '-9223372036854775808', '7', '2014-12-02', null, null, 'ff8080812e33c2cd012e36dcace21105', '', '0', 'dep_1', null, '00000000491404c90149179326950210', '0', '0', 'ff8080813f1aca92013f26bac8b317b6', '2014-11-18-2014-12-02', null, 'ff80808130842ec601309160ce3d2c9a', '3500', '大短期班小学组数学智力谜题之数独(学前一二年级)', '0', '李茂', null, '0', null, null, '2', null, null, null, '数学', null, '2014-10-16 14:10:23', '40288be84ab55a99014ab88943c60089', 'ff8080812d090632012d122a2bff0504', 'ff80808128925b4f01289478912f02ee', null, null, '1', '3', 'level_1', '0', '0', '2014-10-16 14:09:19', '13', '0', '3', '0', '2018-06-14', '1', '2', null, null, '1', '00000000491404c90149179326950211', '1', '亚运村奥亚', '小学组', '2', 'ff80808144b18acb0144b9728d920967', '0', '1', '0', null, '0', '1', '1', '2', null, '0', '奥亚酒店', null, '2014', '0', null, '周二', '大短期班', '1', null, '3467', '1', '2', '2018-06-15', '1', 'ff8080812d090632012d122c160e0509', '周二晚上18:00-20:00', '0', '2018-08-16 17:56:26', '0', null, '0', '4', '0', '朝阳区-北-', '1', '秋短', '晚上', '3f7e0ce4ced511e2b11be41f132f4db0|ff8080813f1aca92013f26bac8b317b6', '52b38a2c22cf94dd0122cfb0f5b600f6', null, '15', '1', '智力谜题之数独(学前一二年级)', null, '1');

insert into `otter`.`tb_curriculum` ( `cuc_subject_id`, `cuc_start_time`, `cuc_class_date`, `cuc_classroom_id`, `cuc_teacher_id`, `cuc_id`, `cuc_tutor_id`, `cuc_room_conflict`, `cuc_classtimeType_id`, `cuc_class_num`, `cuc_print_date`, `cuc_name`, `cuc_changeout_course_num`, `cuc_teacher_type`, `cuc_lock_status`, `cuc_attendance`, `cuc_teacher_conflict`, `cuc_bi_test`, `cuc_modify_time`, `cuc_class_id`, `cuc_classtime_id`, `cuc_cancel`, `cuc_status`, `cuc_classTime_name`, `cuc_tutor_real_name`, `cuc_version`, `cuc_deleted`, `cuc_end_time`, `cuc_changein_course_num`) values ( 'ff80808127d77caa0127d7e10f1c00c4', '18:00:00', '2014-11-18', 'ff8080812d090632012d122c160e0509', 'ff80808130842ec601309160ce3d2c9a', '00000000491404c9014917932557020d', null, null, 'timetype_1', '1', null, null, '2', '0', '1', '1', null, '0', '2018-08-14 17:35:35', '1', 'time_1', '0', '1', '周二晚上18:00-20:00', null, '6', '0', '20:00:00', '3');
-- cuc_1
insert into `otter`.`tb_curriculum` ( `cuc_subject_id`, `cuc_start_time`, `cuc_class_date`, `cuc_classroom_id`, `cuc_teacher_id`, `cuc_id`, `cuc_tutor_id`, `cuc_room_conflict`, `cuc_classtimeType_id`, `cuc_class_num`, `cuc_print_date`, `cuc_name`, `cuc_changeout_course_num`, `cuc_teacher_type`, `cuc_lock_status`, `cuc_attendance`, `cuc_teacher_conflict`, `cuc_bi_test`, `cuc_modify_time`, `cuc_class_id`, `cuc_classtime_id`, `cuc_cancel`, `cuc_status`, `cuc_classTime_name`, `cuc_tutor_real_name`, `cuc_version`, `cuc_deleted`, `cuc_end_time`, `cuc_changein_course_num`) values ( 'ff80808127d77caa0127d7e10f1c00c4', '18:00:00', '2014-11-18', 'ff8080812d090632012d122c160e0509', 'ff80808130842ec601309160ce3d2c9a', 'cuc_1', null, null, 'timetype_1', '1', null, null, '2', '0', '1', '1', null, '0', '2018-08-14 17:35:35', '1', 'time_1', '0', '1', '周二晚上18:00-20:00', null, '6', '0', '20:00:00', '3');
insert into `otter`.`tb_curriculum` ( `cuc_subject_id`, `cuc_start_time`, `cuc_class_date`, `cuc_classroom_id`, `cuc_teacher_id`, `cuc_id`, `cuc_tutor_id`, `cuc_room_conflict`, `cuc_classtimeType_id`, `cuc_class_num`, `cuc_print_date`, `cuc_name`, `cuc_changeout_course_num`, `cuc_teacher_type`, `cuc_lock_status`, `cuc_attendance`, `cuc_teacher_conflict`, `cuc_bi_test`, `cuc_modify_time`, `cuc_class_id`, `cuc_classtime_id`, `cuc_cancel`, `cuc_status`, `cuc_classTime_name`, `cuc_tutor_real_name`, `cuc_version`, `cuc_deleted`, `cuc_end_time`, `cuc_changein_course_num`) values ( 'ff80808127d77caa0127d7e10f1c00c4', '18:00:00', '2014-11-18', 'ff8080812d090632012d122c160e0509', 'ff80808130842ec601309160ce3d2c9a', 'cuc_2', null, null, 'timetype_1', '1', null, null, '2', '0', '1', '1', null, '0', '2018-08-14 17:35:35', '1', 'time_1', '0', '1', '周二晚上18:00-20:00', null, '6', '0', '20:00:00', '3');



-- 校验数据是否可以关联上去；
select * from tb_curriculum where cuc_class_id='1';

select * from tb_class where cla_id in (select cuc_class_id from tb_curriculum where cuc_class_id='1') ;
select * from tb_class_regist_count where crc_class_id in (select cuc_class_id from tb_curriculum where cuc_class_id='1') ;
select * from tb_classtime where ct_id in (select cuc_classtime_id from tb_curriculum where cuc_classtime_id='time_1') ;
select * from tb_classtime_type where ctt_id in (select cuc_classtimeType_id from tb_curriculum where cuc_classtimeType_id='timetype_1') ;
select * from tb_classlevel where lev_id in (select cla_level_id from tb_class where cla_level_id='level_1') ;
select * from tb_department where dept_id in (select cla_servicecenter_id from tb_class where cla_servicecenter_id='dep_1') ;












