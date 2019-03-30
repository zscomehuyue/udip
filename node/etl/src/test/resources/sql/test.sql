DROP TABLE IF EXISTS D_PHONE;
CREATE TABLE D_PHONE (
  ID       BIGINT       NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  NAME     VARCHAR(100) NOT NULL
  COMMENT '主表ID',
  TYPE     VARCHAR(100) NOT NULL,
  USER_ID  BIGINT       NOT NULL
  COMMENT '主键',
  CREATED  TIMESTAMP    NULL
  COMMENT '创建时间',
  MODIFIED TIMESTAMP    NULL
  COMMENT '修改',
  PRIMARY KEY (ID)
)
  ENGINE = INNODB
  DEFAULT CHARSET = UTF8
  COMMENT ='宽表';

DROP TABLE IF EXISTS D_USER;
CREATE TABLE D_USER (
  ID        BIGINT       NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  NAME      VARCHAR(100) NOT NULL
  COMMENT '主表ID',
  AGE       BIGINT       NOT NULL
  COMMENT '宽表名称',
  USER_CARD BIGINT       NOT NULL
  COMMENT '主表主键名称',
  CREATED   TIMESTAMP    NULL
  COMMENT '创建时间',
  MODIFIED  TIMESTAMP    NULL
  COMMENT '修改',
  PRIMARY KEY (ID)
)
  ENGINE = INNODB
  DEFAULT CHARSET = UTF8
  COMMENT ='宽表';

DROP TABLE IF EXISTS D_COURSE;
CREATE TABLE D_COURSE (
  ID       BIGINT       NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  NAME     VARCHAR(100) NOT NULL
  COMMENT '主表ID',
  SCORE    BIGINT       NOT NULL,
  USER_ID  BIGINT       NOT NULL
  COMMENT '主表主键名称',
  CREATED  TIMESTAMP    NULL
  COMMENT '创建时间',
  MODIFIED TIMESTAMP    NULL
  COMMENT '修改',
  PRIMARY KEY (ID)
)
  ENGINE = INNODB
  DEFAULT CHARSET = UTF8
  COMMENT ='宽表';


INSERT INTO D_USER VALUES (1, 'jack', 25, 100020, now(), now());
INSERT INTO D_USER VALUES (2, 'anli', 25, 100010, now(), now());
INSERT INTO D_USER VALUES (3, 'an', 25, 100030, now(), now());
INSERT INTO D_USER VALUES (4, 'meili', 25, 100040, now(), now());
INSERT INTO D_USER VALUES (5, 'meida', 25, 100050, now(), now());
INSERT INTO D_USER VALUES (NULL, 'meida2', 25, 100050, now(), now());


INSERT INTO D_COURSE VALUES (NULL, '语文', 90, 1, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '数学', 90, 1, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '化学', 90, 1, now(), now());

INSERT INTO D_COURSE VALUES (NULL, '语文', 90, 2, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '数学', 90, 2, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '化学', 90, 2, now(), now());

INSERT INTO D_COURSE VALUES (NULL, '语文', 90, 3, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '数学', 90, 3, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '化学', 90, 3, now(), now());

INSERT INTO D_COURSE VALUES (NULL, '语文', 90, 4, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '数学', 90, 4, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '化学', 90, 4, now(), now());


INSERT INTO D_COURSE VALUES (NULL, '语文', 90, 69, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '数学', 90, 69, now(), now());
INSERT INTO D_COURSE VALUES (NULL, '化学', 90, 69, now(), now());
#one to one
INSERT INTO DATA_MEDIA VALUES (300, 'D_USER', 'hwl',
                               '{"mode":"SINGLE","name":"D_USER","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497909000,"gmtModified":1527497909000,"id":1,"name":"hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3306","username":"root"}}',
                               1, now(), now());
INSERT INTO DATA_MEDIA VALUES (301, 'D_USER', 'hwl',
                               '{"mode":"SINGLE","name":"D_USER","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497919000,"gmtModified":1527498165000,"id":2,"name":"target-hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3307","username":"root"}} ',
                               2, now(), now());

INSERT INTO DATA_MEDIA VALUES (500, 'D_PHONE', 'hwl',
                               '{"mode":"SINGLE","name":"D_PHONE","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497909000,"gmtModified":1527497909000,"id":1,"name":"hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3306","username":"root"}}',
                               1, now(), now());
INSERT INTO DATA_MEDIA VALUES (501, 'D_PHONE', 'hwl',
                               '{"mode":"SINGLE","name":"D_PHONE","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497919000,"gmtModified":1527498165000,"id":2,"name":"target-hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3307","username":"root"}} ',
                               2, now(), now());
INSERT INTO DATA_MEDIA VALUES (400, 'udip', 'user_course',
                               '{"mode":"SINGLE","name":"udip","namespace":"user_course","source":{"clusterName":"es","clusterNodes":"127.0.0.1:9300",id:100,"name":"user_course","type":"ES"}} ',
                               100, now(), now());
INSERT INTO load_route VALUES (NULL, 1, 501, 400, 2, 'load_route cl index ', now(), now());
INSERT INTO load_route VALUES (NULL, 1, 301, 400, 2, 'load_route cl index ', now(), now());
INSERT INTO wide_table VALUES (NULL, 'user_course', 301, 'id', 501, 'id', 'USER_ID', 501, 400, '', now(), now());

#one to one


#one to more
# 宽表-es - 模拟
INSERT INTO DATA_MEDIA VALUES (200, 'D_COURSE', 'hwl',
                               '{"mode":"SINGLE","name":"D_COURSE","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497909000,"gmtModified":1527497909000,"id":1,"name":"hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3306","username":"root"}}',
                               1, now(), now());
INSERT INTO DATA_MEDIA VALUES (201, 'D_COURSE', 'hwl',
                               '{"mode":"SINGLE","name":"D_COURSE","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497919000,"gmtModified":1527498165000,"id":2,"name":"target-hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3307","username":"root"}} ',
                               2, now(), now());

INSERT INTO DATA_MEDIA VALUES (300, 'D_USER', 'hwl',
                               '{"mode":"SINGLE","name":"D_USER","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497909000,"gmtModified":1527497909000,"id":1,"name":"hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3306","username":"root"}}',
                               1, now(), now());
INSERT INTO DATA_MEDIA VALUES (301, 'D_USER', 'hwl',
                               '{"mode":"SINGLE","name":"D_USER","namespace":"hwl","source":{"driver":"com.mysql.jdbc.Driver","encode":"UTF8","gmtCreate":1527497919000,"gmtModified":1527498165000,"id":2,"name":"target-hwl","password":"root","type":"MYSQL","url":"jdbc:mysql://127.0.0.1:3307","username":"root"}} ',
                               2, now(), now());


INSERT INTO DATA_MEDIA VALUES (400, 'udip', 'user_course',
                               '{"mode":"SINGLE","name":"udip","namespace":"user_course","source":{"clusterName":"es","clusterNodes":"127.0.0.1:9300",id:100,"name":"user_course","type":"ES"}} ',
                               100, now(), now());

INSERT INTO load_route VALUES (NULL, 1, 201, 400, 2, 'load_route cl index ', now(), now());
INSERT INTO load_route VALUES (NULL, 1, 301, 400, 2, 'load_route cl index ', now(), now());

INSERT INTO wide_table VALUES (NULL, 'user_course', 201, 'id', 301, 'USER_ID', 201, 400, '', now(), now());

#宽表-es - 模拟


DROP TABLE IF EXISTS `classtime_type`;
CREATE TABLE `classtime_type` (
  `id`                   VARCHAR(32) NOT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `name`                 VARCHAR(30)      DEFAULT NULL,
  `order`                INT(11)          DEFAULT NULL,
  `deleted`              INT(11)          DEFAULT '0',
  `create_id`            VARCHAR(32)      DEFAULT NULL,
  `create_date`          TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_id`            VARCHAR(32)      DEFAULT NULL,
  `modify_date`          TIMESTAMP   NULL DEFAULT NULL,
  `timeType_classifyInt` VARCHAR(50)      DEFAULT NULL
  COMMENT '时间类别',
  `timeType_classify`    VARCHAR(100)     DEFAULT NULL
  COMMENT '时间类别',
  `className_use`        INT(11)          DEFAULT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `class_regist_count`;
CREATE TABLE `class_regist_count` (
  `id`           VARCHAR(32) NOT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `id_version`   INT(11)     NOT NULL DEFAULT '0',
  `class_id`     VARCHAR(32)          DEFAULT NULL,
  `regist_count` INT(11)     NOT NULL DEFAULT '0',
  `modify_date`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_class_id` (`class_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id`                          VARCHAR(32) NOT NULL,
  `discriminator`               VARCHAR(20)          DEFAULT NULL,
  `name`                        VARCHAR(50)          DEFAULT NULL,
  `parent_id`                   VARCHAR(32)          DEFAULT NULL,
  `is_serv`                     INT(11)              DEFAULT '0',
  `create_date`                 TIMESTAMP   NULL     DEFAULT CURRENT_TIMESTAMP,
  `master`                      VARCHAR(32)          DEFAULT NULL,
  `master_tel`                  VARCHAR(20)          DEFAULT NULL,
  `second_person`               VARCHAR(32)          DEFAULT NULL,
  `second_person_tel`           VARCHAR(20)          DEFAULT NULL,
  `phone`                       VARCHAR(20)          DEFAULT NULL,
  `address`                     VARCHAR(200)         DEFAULT NULL,
  `areaid`                      VARCHAR(32)          DEFAULT NULL,
  `order`                       VARCHAR(30)          DEFAULT NULL,
  `serv_order`                  INT(11)              DEFAULT NULL,
  `terminal_no`                 VARCHAR(50)          DEFAULT NULL
  COMMENT '终端号',
  `code`                        VARCHAR(20)          DEFAULT NULL
  COMMENT '编号',
  `city_id`                     VARCHAR(32)          DEFAULT NULL
  COMMENT '城市',
  `is_show_student`             INT(2)               DEFAULT '0'
  COMMENT '是否在家校平台显示(0：不显示；1：显示；)',
  `is_show_teacher`             INT(2)               DEFAULT '0'
  COMMENT '是否在教师平台显示(0：不显示；1：显示；)',
  `creater_id`                  VARCHAR(32)          DEFAULT NULL,
  `is_receive`                  INT(2)               DEFAULT '0',
  `deleted`                     INT(2)               DEFAULT '0',
  `disable`                     INT(2)               DEFAULT '0',
  `longitude`                   DOUBLE               DEFAULT NULL,
  `latitude`                    DOUBLE               DEFAULT NULL,
  `serv_address`                VARCHAR(500)         DEFAULT NULL,
  `is_call_service_center`      INT(11)              DEFAULT '0',
  `first_char`                  VARCHAR(20)          DEFAULT NULL
  COMMENT '服务中心首字母',
  `district_id`                    VARCHAR(32)          DEFAULT NULL,
  `mobile_alias`                VARCHAR(80)          DEFAULT NULL
  COMMENT 'M站别名',
  `mobile_area_id`              VARCHAR(32)          DEFAULT NULL
  COMMENT '所属M站地区',
  `is_show_in_mobile`           INT(11)              DEFAULT '0'
  COMMENT '是否在M站显示',
  `business_reimbursement_date` TIMESTAMP   NOT NULL DEFAULT '0000-00-00 00:00:00',
  `simple_name`                 VARCHAR(50)          DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_longitude` (`longitude`),
  KEY `index_latitude` (`latitude`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `class_level`;
CREATE TABLE `class_level` (
  `id`                   VARCHAR(32)  NOT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `name`                 VARCHAR(60)           DEFAULT NULL,
  `order`                INT(11)               DEFAULT '0',
  `display_name`         VARCHAR(60)           DEFAULT NULL,
  `degree`               DOUBLE(4, 2) NOT NULL,
  `parent_id`            VARCHAR(32)           DEFAULT '0',
  `type`                 INT(11)               DEFAULT '0',
  `deleted`              INT(11)               DEFAULT '0',
  `create_date`          TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP,
  `creater_id`           VARCHAR(32)           DEFAULT NULL,
  `modify_date`          TIMESTAMP    NULL     DEFAULT NULL,
  `modify_id`            VARCHAR(32)           DEFAULT NULL,
  `status`               INT(11)               DEFAULT '0',
  `is_leaf`              INT(11)      NOT NULL DEFAULT '1',
  `term_is_show_student` INT(11)               DEFAULT '0',
  `term_is_show_teacher` INT(11)               DEFAULT '0',
  `is_mobi`              INT(4)                DEFAULT '0'
  COMMENT '屏蔽摩比数据',
  `is_live_lesson`       INT(11)               DEFAULT '0',
  `disable`              INT(11)               DEFAULT '0'
  COMMENT '状态：0-可用，1-停用',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `classtime`;
CREATE TABLE `classtime` (
  `id`              VARCHAR(32) NOT NULL,
  `city_id`         VARCHAR(10)      not NULL DEFAULT '0',
  `type_id`         VARCHAR(32)      DEFAULT NULL,
  `time_name`       VARCHAR(200)     DEFAULT NULL,
  `start_time`      TIME             DEFAULT NULL,
  `end_time`        TIME             DEFAULT NULL,
  `class_hours`     DOUBLE           DEFAULT NULL,
  `is_show_teacher` INT(11)          DEFAULT '0',
  `order`           INT(11)          DEFAULT NULL,
  `deleted`         INT(11)          DEFAULT '0',
  `create_id`       VARCHAR(32)      DEFAULT NULL,
  `create_date`     TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_id`       VARCHAR(32)      DEFAULT NULL,
  `modify_date`     DATETIME         DEFAULT NULL,
  `period`          VARCHAR(20)      DEFAULT NULL
  COMMENT '时段(上午,下午,晚上)',
  `period_type`     INT(11)          DEFAULT NULL
  COMMENT '时段类型(1：上午,2：下午,3：晚上)',
  `status`          INT(11)          DEFAULT '0'
  COMMENT ' 0 正常；1 停用',
  PRIMARY KEY (`id`),
  KEY `index_type_id` (`type_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;




DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id` varchar(32) NOT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `teacher_name` varchar(200) DEFAULT NULL,
  `real_name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `login_name` varchar(20) DEFAULT NULL,
  `login_pwd` varchar(20) DEFAULT NULL,
  `teacher_code` varchar(20) DEFAULT NULL,
  `identify_num` varchar(20) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `ismarraied` int(11) DEFAULT NULL,
  `title` varchar(30) DEFAULT NULL,
  `picture_url` varchar(255) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `post` varchar(10) DEFAULT NULL,
  `nation_id` varchar(32) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `birthpalce_city_id` varchar(32) DEFAULT NULL,
  `birthpalce` varchar(300) DEFAULT NULL,
  `address_city_id` varchar(32) DEFAULT NULL,
  `address` varchar(300) DEFAULT NULL,
  `graduation_school` varchar(300) DEFAULT NULL,
  `graduation_school_city_id` varchar(32) DEFAULT NULL,
  `school_startdate` date DEFAULT NULL,
  `school_enddate` date DEFAULT NULL,
  `profession` varchar(100) DEFAULT NULL,
  `school_degree` int(4) DEFAULT NULL,
  `originalschoolorganization_name` varchar(100) DEFAULT NULL,
  `nowschoolorganization_name` varchar(100) DEFAULT NULL,
  `organization_startdate` date DEFAULT NULL,
  `organization_enddate` date DEFAULT NULL,
  `old_teach_subject` varchar(50) DEFAULT NULL,
  `old_teach_result` text,
  `old_classfees` varchar(20) DEFAULT NULL,
  `features` text,
  `experience` text,
  `comment` text,
  `teach_result` text,
  `teach_year` date DEFAULT NULL,
  `people_amount_gap_id` varchar(32) DEFAULT NULL,
  `jobtype_id` varchar(32) DEFAULT NULL,
  `joindate` date DEFAULT NULL,
  `contract_type` varchar(32) DEFAULT NULL,
  `contract_area` varchar(32) DEFAULT NULL,
  `subject_id` varchar(32) DEFAULT NULL,
  `resume_id` varchar(32) DEFAULT NULL,
  `wagelevel` varchar(32) DEFAULT NULL,
  `degree_id` varchar(32) DEFAULT NULL,
  `term_type` int(11) DEFAULT NULL,
  `hourfee` varchar(20) DEFAULT NULL,
  `voluntary_hour` varchar(20) DEFAULT NULL,
  `voluntary_gt_id` varchar(32) DEFAULT NULL,
  `deposit` int(4) DEFAULT NULL,
  `awards` int(4) DEFAULT NULL,
  `state` int(11) DEFAULT '0',
  `teacher_cardnumber` varchar(200) DEFAULT NULL,
  `teacher_cardnumber_password` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `teacher_new_pwd` varchar(50) NOT NULL DEFAULT '' COMMENT '教师新登录密码(MD5),旧密码教师重构二期后废弃',
  `teacher_wage_password` varchar(50) DEFAULT NULL,
  `accountlocation` varchar(300) DEFAULT NULL,
  `accountlocation_city_id` varchar(32) DEFAULT NULL,
  `teacher_emergency_contacts` varchar(20) DEFAULT NULL,
  `teacher_emergency_contacts_phone` varchar(20) DEFAULT NULL,
  `teacher_home_phone` varchar(20) DEFAULT NULL,
  `contract_startdate` date DEFAULT NULL,
  `contract_enddate` date DEFAULT NULL,
  `fulltime_appointment_date` date DEFAULT NULL,
  `socialsecurity_payment_date` date DEFAULT NULL,
  `fund_payment_date` date DEFAULT NULL,
  `radix` int(11) DEFAULT NULL,
  `bank_id` varchar(32) DEFAULT NULL,
  `other_bank` varchar(50) DEFAULT NULL,
  `competition_exp` text,
  `original_seniority` int(11) DEFAULT NULL,
  `nowschool_cityzone` varchar(20) DEFAULT NULL,
  `startend_time` varchar(20) DEFAULT NULL,
  `statusquo_complement` text,
  `creater_id` varchar(32) DEFAULT NULL,
  `info_collect_type_id` varchar(32) DEFAULT NULL,
  `isrecommend` int(11) DEFAULT NULL,
  `school_appraise` text,
  `info_content` text,
  `teaching_materials` varchar(100) DEFAULT NULL,
  `teaching_materials_positive` int(11) DEFAULT NULL,
  `scheduling` varchar(100) DEFAULT NULL,
  `scheduling_positive` int(11) DEFAULT NULL,
  `activity_organization` varchar(100) DEFAULT NULL,
  `activity_organization_positive` int(11) DEFAULT NULL,
  `cultivate` varchar(100) DEFAULT NULL,
  `cultivate_positive` int(11) DEFAULT NULL,
  `teacher_care` varchar(100) DEFAULT NULL,
  `teacher_care_positive` int(11) DEFAULT NULL,
  `subject_publicize` varchar(100) DEFAULT NULL,
  `subject_publicize_positive` int(11) DEFAULT NULL,
  `student_manage` varchar(100) DEFAULT NULL,
  `student_manage_positive` int(11) DEFAULT NULL,
  `pay_positive` int(11) DEFAULT NULL,
  `jump_pay_positive` int(11) DEFAULT NULL,
  `Platform_use_positive` int(11) DEFAULT NULL,
  `other_content_type` text,
  `birthday_properties` int(11) DEFAULT NULL,
  `birthday_remarks` varchar(100) DEFAULT NULL,
  `profess_type_id` varchar(32) DEFAULT NULL,
  `profess_department` varchar(50) DEFAULT NULL,
  `source_id` varchar(32) DEFAULT NULL,
  `teaching_point_services_positive` int(11) DEFAULT NULL,
  `appointment_date` date DEFAULT NULL,
  `createdate` date DEFAULT NULL,
  `school_degree_name` varchar(20) DEFAULT NULL,
  `hebin1` varchar(100) DEFAULT NULL,
  `hebin2` varchar(100) DEFAULT NULL,
  `hebin3` varchar(100) DEFAULT NULL,
  `hebin4` varchar(100) DEFAULT NULL,
  `modifydate` date DEFAULT NULL,
  `popularize` int(11) DEFAULT '0',
  `is_web_show` int(11) DEFAULT '0',
  `popularize_date` date DEFAULT NULL,
  `motto` text,
  `introduction` text,
  `login_start` datetime DEFAULT NULL,
  `login_count` int(8) DEFAULT NULL,
  `social_security_radix` int(11) DEFAULT NULL,
  `video_url` text,
  `issign_zk` int(11) DEFAULT '0',
  `issign_wx` int(11) DEFAULT '0',
  `random_password` varchar(10) DEFAULT '0',
  `randpwd_time` datetime DEFAULT NULL,
  `position_type` int(11) NOT NULL DEFAULT '0' COMMENT '教师职位（0普通教师，1班主任）',
  `lesson` int(11) DEFAULT '0',
  `firstChar` varchar(100) DEFAULT NULL,
  `allChar` varchar(100) DEFAULT NULL,
  `classtype` varchar(10) DEFAULT NULL COMMENT '教师所带班型（在数据库中保存以,进行分割）',
  `name_pinyin` varchar(32) DEFAULT NULL,
  `tags` varchar(1000) DEFAULT NULL,
  `app_message_subjects_code` bigint(20) DEFAULT NULL,
  `emp_no` varchar(10) DEFAULT NULL COMMENT '员工编号',
  `double_teacher_state` int(1) DEFAULT NULL COMMENT '是否双师（0否，1是）',
  `common_teacher_state` int(1) DEFAULT NULL COMMENT '是否普通（0否，1是）',
  `live_teacher_state` int(11) DEFAULT '0' COMMENT '是否在线（0不是，1是）',
  `enterprise_email` varchar(50) DEFAULT NULL COMMENT '企业邮箱',
  `positions` varchar(50) DEFAULT NULL COMMENT '教师岗位',
  `create_type` int(1) DEFAULT NULL COMMENT '创建类型（1系统创建，2人工创建）',
  `picture_url_oss` varchar(255) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL COMMENT '主讲个人绑定手机号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




-- ----------------------------
--  Table structure for `tb_tutor`
-- ----------------------------
DROP TABLE IF EXISTS `tutor`;
CREATE TABLE `tutor` (
  `id` varchar(32) NOT NULL COMMENT '辅导教师id',
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `RealName` varchar(200) DEFAULT NULL,
  `sysName` varchar(200) DEFAULT NULL,
  `sex` int(1) DEFAULT NULL COMMENT '0代表女,1代表男',
  `code` varchar(50) DEFAULT NULL,
  `birthday` date DEFAULT NULL COMMENT '生日',
  `password` varchar(50) DEFAULT NULL,
  `nation` varchar(50) DEFAULT NULL,
  `nation_Id` varchar(50) DEFAULT NULL COMMENT '民族id待用',
  `homeTown` varchar(255) DEFAULT NULL COMMENT '籍贯',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `entryDate` date DEFAULT NULL COMMENT '入职日期',
  `mobile` varchar(100) DEFAULT NULL COMMENT '移动电话',
  `familyPhone` varchar(100) DEFAULT NULL COMMENT '家庭电话',
  `emergencyPhone` varchar(100) DEFAULT NULL COMMENT '紧急联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `emergencyPerson` varchar(255) DEFAULT NULL COMMENT '紧急联系人',
  `ismarraied` varchar(255) DEFAULT NULL COMMENT '0未结婚.1已结婚',
  `app_message_subjects_code` bigint(255) NOT NULL DEFAULT '0' COMMENT 'app科目位运算后id',
  `message_subjects_code` bigint(20) DEFAULT '0' COMMENT '课程运算后i位id',
  `imgUrl` varchar(255) DEFAULT NULL COMMENT '照片地址,根目录+id',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `createUserId` varchar(255) DEFAULT NULL COMMENT '创建人',
  `modifyTime` datetime DEFAULT NULL COMMENT '最新的修改时间',
  `isfreeze` varchar(20) DEFAULT NULL COMMENT '0未冻结,1冻结状态',
  `delete` varchar(20) DEFAULT '0' COMMENT '0启用,1弃用,预留字段未应用',
  `jobType` int(3) DEFAULT NULL,
  `grades` bigint(20) DEFAULT '0',
  `teacherType` varchar(20) DEFAULT '1' COMMENT '辅导老师来源(1：直播o2o；2：在线直播；)',
  `teacherBrief` varchar(255) DEFAULT NULL COMMENT '辅导老师简介',
  `firstChar` char(1) DEFAULT NULL COMMENT '姓名首字母',
  `features` text COMMENT '教学特点',
  `teach_result` text COMMENT '教学成果',
  `experience` text COMMENT '教学经验',
  `comment` text COMMENT '评语',
  `graduation_school_city_id` varchar(32) DEFAULT NULL COMMENT '毕业院校城市id',
  `graduation_school` varchar(300) DEFAULT NULL COMMENT '毕业院校',
  `school_startdate` date DEFAULT NULL COMMENT '起始时间',
  `school_enddate` date DEFAULT NULL COMMENT '结束时间',
  `profession` varchar(100) DEFAULT NULL COMMENT '专业',
  `turot_school_degree` int(4) DEFAULT NULL COMMENT '学历',
  `competition_exp` text COMMENT '竞赛经验',
  `emp_no` varchar(20) DEFAULT NULL COMMENT '员工编号',
  `live_teacher_state` bigint(20) DEFAULT NULL COMMENT '是否在线老师（0否，1是）',
  `enterprise_email` varchar(50) DEFAULT NULL COMMENT '企业邮箱',
  `img_url_oss` varchar(255) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL COMMENT '辅导个人绑定手机号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




DROP TABLE IF EXISTS `class_regist_count`;
CREATE TABLE `class_regist_count` (
  `id`           VARCHAR(32) NOT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `id_version`   INT(11)     NOT NULL DEFAULT '0',
  `class_id`     VARCHAR(32)          DEFAULT NULL,
  `regist_count` INT(11)     NOT NULL DEFAULT '0',
  `modify_date`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_class_id` (`class_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `classlevel`;
CREATE TABLE `classlevel` (
  `id`                   VARCHAR(32)  NOT NULL,
  `name`                 VARCHAR(60)           DEFAULT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `order`                INT(11)               DEFAULT '0',
  `display_name`         VARCHAR(60)           DEFAULT NULL,
  `degree`               DOUBLE(4, 2) NOT NULL,
  `parent_id`            VARCHAR(32)           DEFAULT '0',
  `type`                 INT(11)               DEFAULT '0',
  `deleted`              INT(11)               DEFAULT '0',
  `create_date`          TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP,
  `creater_id`           VARCHAR(32)           DEFAULT NULL,
  `modify_date`          TIMESTAMP    NULL     DEFAULT NULL,
  `modify_id`            VARCHAR(32)           DEFAULT NULL,
  `status`               INT(11)               DEFAULT '0',
  `is_leaf`              INT(11)      NOT NULL DEFAULT '1',
  `term_is_show_student` INT(11)               DEFAULT '0',
  `term_is_show_teacher` INT(11)               DEFAULT '0',
  `is_mobi`              INT(4)                DEFAULT '0'
  COMMENT '屏蔽摩比数据',
  `is_live_lesson`       INT(11)               DEFAULT '0',
  `disable`              INT(11)               DEFAULT '0'
  COMMENT '状态：0-可用，1-停用',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `classtime`;
CREATE TABLE `classtime` (
  `id` varchar(32) NOT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `type_id` varchar(32) DEFAULT NULL,
  `time_name` varchar(200) DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `class_hours` double DEFAULT NULL,
  `is_show_teacher` int(11) DEFAULT '0',
  `order` int(11) DEFAULT NULL,
  `deleted` int(11) DEFAULT '0',
  `create_id` varchar(32) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_id` varchar(32) DEFAULT NULL,
  `modify_date` datetime DEFAULT NULL,
  `period` varchar(20) DEFAULT NULL COMMENT '时段(上午,下午,晚上)',
  `period_type` int(11) DEFAULT NULL COMMENT '时段类型(1：上午,2：下午,3：晚上)',
  `status` int(11) DEFAULT '0' COMMENT ' 0 正常；1 停用',
  PRIMARY KEY (`id`),
  KEY `index_type_id` (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `classtime_type`;
CREATE TABLE `classtime_type` (
  `id`                   VARCHAR(32) NOT NULL,
  `name`                 VARCHAR(30)      DEFAULT NULL,
  `city_id`              VARCHAR(10)      not NULL DEFAULT '0',
  `order`                INT(11)          DEFAULT NULL,
  `deleted`              INT(11)          DEFAULT '0',
  `create_id`            VARCHAR(32)      DEFAULT NULL,
  `create_date`          TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_id`            VARCHAR(32)      DEFAULT NULL,
  `modify_date`          TIMESTAMP   NULL DEFAULT NULL,
  `timeType_classifyInt` VARCHAR(50)      DEFAULT NULL
  COMMENT '时间类别',
  `timeType_classify`    VARCHAR(100)     DEFAULT NULL
  COMMENT '时间类别',
  `className_use`        INT(11)          DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `change_course_amount`;
CREATE TABLE `change_course_amount` (
  `id` varchar(32) NOT NULL COMMENT 'id',
  `city_id` varchar(32)  NULL,
  `class_type` int(11) DEFAULT NULL COMMENT '授课类型',
  `grade_type_id` varchar(32) DEFAULT NULL COMMENT '年部id',
  `grade_type_name` varchar(50) DEFAULT NULL COMMENT '年部名称',
  `grade_id` varchar(32) DEFAULT NULL COMMENT '年级id',
  `grade_name` varchar(50) DEFAULT NULL COMMENT '年级名称',
  `change_course_amount` int(11) DEFAULT NULL COMMENT '可多调人数限额',
  `isdeleted` int(11) DEFAULT '0' COMMENT '是否删除',
  `creater_id` varchar(32) DEFAULT NULL COMMENT '创建人',
  `creater_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modifier_id` varchar(32) DEFAULT NULL COMMENT '修改人',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `system_param`;
CREATE TABLE `system_param` (
  `id` varchar(32) NOT NULL,
  `city_id` varchar(32)  NULL,
  `param_name` varchar(100) DEFAULT NULL,
  `param_alias` varchar(100) DEFAULT NULL,
  `param_value` varchar(100) DEFAULT NULL,
  `deleted` int(11) DEFAULT '0',
  `creater_id` varchar(32) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `version` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_param_alias` (`param_alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `regist`;
CREATE TABLE `regist` (
  `id` varchar(32) NOT NULL,
  `city_id` varchar(32)  NULL,
  `class_id` varchar(32) DEFAULT NULL,
  `iscontinue` int(11) DEFAULT NULL,
  `isdeleted` int(11) DEFAULT '0',
  `student_id` varchar(32) DEFAULT NULL,
  `ispay` int(11) DEFAULT '0',
  `way` int(11) DEFAULT NULL,
  `class_no` int(11) DEFAULT NULL,
  `add_class_quantity` int(11) DEFAULT '0',
  `pay_enddate` datetime DEFAULT NULL,
  `source_class_id` varchar(32) DEFAULT NULL,
  `is_test` int(11) DEFAULT '0',
  `is_statistic` int(11) NOT NULL DEFAULT '1',
  `listener_code` varchar(100) DEFAULT NULL,
  `listenercard_id` varchar(32) DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_id` varchar(32) DEFAULT NULL,
  `remark` varchar(800) DEFAULT NULL,
  `version` int(11) DEFAULT '0',
  `is_newstu` int(11) NOT NULL DEFAULT '0',
  `passedcount` int(11) NOT NULL DEFAULT '0',
  `isbankpay` int(10) DEFAULT '0',
  `reserved_times` int(11) NOT NULL DEFAULT '0' COMMENT '记录预留转班次数',
  `is_reserved` int(11) NOT NULL DEFAULT '0' COMMENT '是否预留转班（0否，1是）',
  `reserve_source_class_id` varchar(32) DEFAULT NULL COMMENT '预转班源班级ID',
  `reserved_change_classes_deadline` datetime DEFAULT NULL,
  `is_receipt_books` int(2) DEFAULT '0' COMMENT '是否领取书0:未领取1:领取',
  `modify_id` varchar(32) DEFAULT NULL COMMENT '修改人id',
  `modify_date` timestamp NULL DEFAULT NULL COMMENT '修改日期',
  `bi_test` int(11) DEFAULT '0' COMMENT '是否为测试数据0：否；1：是',
  `pay_date` datetime DEFAULT NULL,
  `candidate_no` varchar(12) DEFAULT NULL,
  `candidate_serviceCenter_id` varchar(32) DEFAULT NULL,
  `seat` int(11) DEFAULT NULL,
  `is_send_sms` int(11) DEFAULT '0',
  `beili_course_id` varchar(32) DEFAULT NULL COMMENT '在线外教排课id',
  `is_beili_course_no` int(11) DEFAULT '0' COMMENT '是否选课',
  `crm_statistic` int(11) NOT NULL DEFAULT '0',
  `activity` varchar(255) DEFAULT NULL COMMENT '报名渠道-活动名称',
  `associated_common_class_type` int(10) DEFAULT NULL COMMENT '限制方式 0:强制 1:推荐 2:无限制',
  `associated_common_class_id` varchar(32) DEFAULT NULL COMMENT '配对的普通班报名ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `district`;
CREATE TABLE `district` (
  `id` varchar(32) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `link_url` varchar(200) DEFAULT NULL,
  `order` int(11) DEFAULT NULL,
  `code` varchar(4) DEFAULT NULL,
  `city_id` varchar(32) DEFAULT NULL,
  `deleted` int(11) DEFAULT '0',
  `creater_id` varchar(32) DEFAULT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



DROP PROCEDURE if EXISTS addtk;
DELIMITER $$
CREATE PROCEDURE addtk(maxCount int)
  begin
    DECLARE i int(11);
    set autocommit= 0;
    SET i = 1;
    WHILE i <= maxCount DO
      INSERT IGNORE INTO `tb_curriculum` (`cuc_subject_id`, `cuc_start_time`, `cuc_class_date`, `cuc_classroom_id`, `cuc_teacher_id`, `cuc_id`, `cuc_tutor_id`, `cuc_room_conflict`, `cuc_classtimeType_id`, `cuc_class_num`, `cuc_print_date`, `cuc_name`, `cuc_changeout_course_num`, `cuc_teacher_type`, `cuc_lock_status`, `cuc_attendance`, `cuc_teacher_conflict`, `cuc_bi_test`, `cuc_modify_time`, `cuc_class_id`, `cuc_classtime_id`, `cuc_cancel`, `cuc_status`, `cuc_classTime_name`, `cuc_tutor_real_name`, `cuc_version`, `cuc_deleted`, `cuc_end_time`, `cuc_changein_course_num`)
      VALUES (concat('tb_curriculum_', i)
        , '18:00:00'
        , '2014-11-18'
        , concat('cuc_classroom_id_', i)
        , concat('cuc_teacher_id_', i)
        , concat('cuc_id_', i)
        , NULL, NULL
        , concat('cuc_classtimeType_id_', i), '1', NULL, NULL, '2', '0', '1', '1', NULL, '0', now()
        , concat('cla_id_', i)
        , concat('cuc_classtime_id_', i), '0', '1', '周二晚上18:00-20:00', NULL, '6', '0', '20:00:00', '3');



      insert IGNORE into `tb_class` ( `cla_servicecenter_address`, `cla_create_type`, `cla_tutor_id`, `cla_class_change_name`
        , `cla_version`, `cla_gt_name`, `class_introduction`, `class_introduction_modifier`
        , `cla_add_max_persons`, `cla_start_date`, `cla_classroom_name`, `cla_term_id`
        , `cla_subject_ids`, `cla_frequency`, `cla_end_date`, `cla_class_change_id`
        , `cla_exam_id`, `cla_atte_stat_type_id`, `cla_exclude_date`, `cla_is_expired`
        , `cla_servicecenter_id`, `cla_source_class`, `cla_classdate_id`, `cla_is_test`
        , `cla_is_beili_build`, `cla_classtime_ids`, `cla_classdate_name`
        , `cla_tutor_real_name`, `cla_teacher_ids`, `cla_price`, `cla_name`
        , `cla_status`, `cla_teacher_names`, `cla_live_class_template_id`
        , `cla_is_live_class`, `cla_admin_status`, `cla_recommend_number`
        , `cla_feetype`, `cla_live_class_time_id`, `cla_nickname`
        , `cla_teacher_conflict`, `cla_subject_names`, `cla_haspersons`
        , `cla_audit_date`, `cla_lock_log_id`, `cla_venue_id`, `cla_auditor_id`
        , `cla_serial_no`, `cla_modify_id`, `cla_isdisplay_front`, `cla_class_count`
        , `cla_level_id`, `cla_is_close`, `cla_xes_order`, `cla_create_date`
        , `cla_grade_id`, `is_push_to_xes_im`, `cla_passed_count`
        , `cla_is_mobi`, `cla_add_max_start`, `cla_is_statistic`
        , `cla_courseware_send`, `class_introduction_modified_time`
        , `cla_recommend_number_MD5`, `cla_deleted`, `cla_template_id`
        , `cla_id`, `cla_servicecenter_name`, `cla_grade_name`, `cla_gt_id`
        , `cla_creater_id`, `cla_is_collection_charges`, `cla_headquarters_openclass`
        , `cla_is_schooltest`, `cla_alternate_persons`, `cla_is_recommend`
        , `cla_isdisplay_teacher`, `cla_curr_lock_status`, `cla_audit_status`
        , `cla_pay_end_date`, `cla_is_beili`, `cla_venue_name`, `cla_live_class_type`
        , `cla_year`, `cla_is_double_teacher_live_class`, `cla_class_teacher_name`
        , `cla_classtime_timeClassify`, `cla_term_name`, `cla_is_pay`, `cla_class_teacher_id`
        , `cla_teacher_codes`, `cla_is_fee`, `cla_every_hours`, `cla_add_max_end`, `cla_is_attend`
        , `cla_classroom_id`, `cla_classtime_names`, `cla_bi_test`, `cla_modify_date`, `cla_is_push_to_xes_im`
        , `cla_course_id`, `cla_biz_type`, `cla_class_type`, `cla_xes_surplus`, `cla_area_name`
        , `cla_isdisplay_student`, `cla_atte_stat_type_name`, `cla_classtime_timePeriod`, `cla_classTime_Type_Id`
        , `cla_area_id`, `cla_room_conflict`, `cla_max_persons`, `cla_is_web_regist`, `cla_level_name`
        , `cla_live_number_id`, `cla_is_hidden`)
      values ( null, '0', concat('cla_tutor_id_', i), null, null, '小学部', null, null, '3', '2014-11-18', '奥亚酒店403', '5'
        , '-9223372036854775808', '7', '2014-12-02', null, null
        ,concat('cla_atte_stat_type_id_', i), '', '0'
        , concat('dept_id_',i), null
        , concat('cla_classdate_id_',i), '0', '0'
        , concat('cla_classtime_ids_',i), '2014-11-18-2014-12-02', null
        , concat('cla_teacher_ids_',i), '3500', '大短期班小学组数学智力谜题之数独(学前一二年级)'
        , '0', '李茂', null, '0', null, null, '2'
        , null, null, null, '数学', null, '2014-10-16 14:10:23'
        , concat('cla_lock_log_id_',i)
        , concat('cla_venue_id_',i)
        , concat('cla_auditor_id_',i)
        , null, null, '1', '3'
        , concat('level_id_',i)
        , '0', '0', '2014-10-16 14:09:19', '13', '0', '3', '0', '2018-06-14', '1', '2', null, null, '1'
        , concat('cla_template_id_',i)
        , concat('cla_id_',i)
        , '亚运村奥亚', '小学组', '2'
        , concat('cla_creater_id_',i), '0', '1', '0', null
        , '0', '1', '1', '2', null, '0', '奥亚酒店'
        , null, '2014', '0', null, '周二', '大短期班', '1', null, '3467', '1', '2'
        , '2018-06-15', '1'
        , concat('cla_classroom_id_',i)
        , '周二晚上18:00-20:00', '0', '2018-07-01 17:55:38', '0', null, '0', '4', '0', '朝阳区-北-', '1', '秋短', '晚上'
        , concat('cla_classTime_Type_Id_',i)
        , concat('cla_area_id_',i), null, '15', '1', '智力谜题之数独(学前一二年级)', null, '1');


      insert IGNORE into `tb_department` ( `dept_mobile_alias`, `dept_is_show_student`
        , `dept_id`, `dept_master`
        , `dept_is_call_service_center`, `dept_is_receive`, `dept_create_date`, `dept_latitude`
        , `dept_is_show_teacher`, `dept_serv_order`, `dept_master_tel`, `dept_areaid`
        , `dept_city_id`, `dept_deleted`, `dept_name`, `dept_business_reimbursement_date`
        , `dept_parent_id`, `dept_mobile_area_id`, `dept_second_person`, `dept_disable`
        , `dept_first_char`, `dept_district_id`, `dept_serv_address`, `dept_longitude`
        , `dept_second_person_tel`, `dept_discriminator`, `dept_creater_id`, `dept_is_serv`
        , `dept_code`, `dept_simple_name`, `dept_address`, `dept_is_show_in_mobile`
        , `dept_phone`, `dept_order`, `dept_terminal_no`) values
        ( null, '1', concat('dept_id_',i), concat('dept_id_',i), '1'
          , '0', '2014-10-30 08:52:07', '39.989495', '1', null, '111'
          , concat('cla_area_id_',i), '110100', '0', '惠新西街'
          , '2018-08-11 00:00:00', 'ff8080812819ee6501281a58338500ac'
          , null, 'ff80808138716e7e01388dce557f32d7', '0', 'HXXJ', '110105'
          , '北京市朝阳区干杨树街甲16号中润珠宝城三层123', '116.418132', '111'
          , 'servicecenter', '52b38a251d6c3d15011d71d113c30441', '1'
          , '181', '惠新西街', '北京市朝阳区干杨树街甲16号中润珠宝城三层123'
          , '0', '12121213131', '614048046120', '98561235');

       insert IGNORE into `tb_class_regist_count` ( `crc_regist_count`, `crc_id`, `crc_modify_date`, `crc_id_version`, `crc_class_id`) values
        ( '13', concat('regist_count_',i), '2014-11-16 17:53:25', '27', concat('cla_id_',i));


      insert IGNORE into `tb_classlevel` ( `lev_id`, `lev_parent_id`, `lev_create_date`, `lev_status`, `lev_name`, `lev_is_mobi`, `is_live_lesson`, `lev_disable`, `lev_modify_id`, `lev_order`, `lev_modify_date`, `term_is_show_teacher`, `lev_display_name`, `lev_degree`, `term_is_show_student`, `lev_type`, `lev_creater_id`, `lev_deleted`, `lev_is_leaf`) values
        ( concat('level_id_',i), '0', '2014-10-02 16:36:08', '0', '北京移动端测试G班(测试勿报名)', '0', '0', '1', '52b38a251d6c3d15011d71d113c30441', '1111', '2014-10-02 16:36:08', '0', null, '1.00', '1', '1', '52b38a251d6c3d15011d71d113c30441', '0', '1');



      insert IGNORE into tb_classtime_type ( `ctt_id`, `ctt_create_id`, `ctt_order`, `ctt_deleted`, `ctt_name`, `ctt_modify_id`, `ctt_create_date`, `ctt_modify_date`, `ctt_timeType_classifyInt`, `ctt_timeType_classify`, `ctt_className_use`)
      values ( concat('cuc_classtimeType_id_',i), null, '11', '0', '2.1-7', null, '2014-11-01 11:24:29', '2015-01-21 14:14:48', '1,2,3,4,5,6,7', '周一,周二,周三,周四,周五,周六,周日', '1');


      set i=i+1;
      if i%100=0
      then
        commit;
      end if ;
    END WHILE;
    commit;

  end$$
DELIMITER ;






call addtk(1,10) ;


truncate table tb_curriculum ;
truncate table tb_class ;
truncate table tb_department ;
truncate table tb_class_regist_count ;
truncate table tb_classlevel ;
truncate table tb_classtime_type ;



select count(1) from tb_curriculum ;

select count(1) from tb_class ;

select count(1) from tb_department ;

select count(1) from tb_class_regist_count ;

select count(1) from tb_classlevel ;

select count(1) from tb_classtime_type ;

select count(1) from tb_teacher ;

select count(1) from tb_system_param;

select count(1) from tb_district ;





# //////////////////////////
create table otter.tb_test as  select c.* from beijing_xxgl.tb_class b , beijing_xxgl.tb_curriculum c where b.cla_id=c.cuc_class_id and b.cla_create_date>'2018' limit 20000 ;
insert  IGNORE into  otter.tb_curriculum   select * from  otter.tb_test ;

insert  IGNORE into  otter.tb_classtime    select * from beijing_xxgl.tb_classtime where ct_id in (select cuc_classtime_id from otter.tb_curriculum);

insert  IGNORE into  otter.tb_class    select * from beijing_xxgl.tb_class where cla_id in (select cuc_class_id from otter.tb_curriculum);

insert  IGNORE into  otter.tb_class_regist_count    select * from beijing_xxgl.tb_class_regist_count where crc_class_id in (select cuc_class_id from otter.tb_curriculum);

insert  IGNORE into  otter.tb_department    select * from beijing_xxgl.tb_department where dept_id in (select cla_servicecenter_id from otter.tb_class);

insert  IGNORE into  otter.tb_classtime_type    select * from beijing_xxgl.tb_classtime_type where ctt_id in (select cuc_classtimeType_id from otter.tb_curriculum);

insert  IGNORE into  otter.tb_classlevel    select * from beijing_xxgl.tb_classlevel where lev_id in (select cuc_classtimeType_id from otter.tb_class);

insert  IGNORE into  otter.tb_classlevel    select * from beijing_xxgl.tb_classlevel where lev_id in (select cla_level_id from otter.tb_class);

insert  IGNORE into  otter.tb_classtime    select * from beijing_xxgl.tb_classtime where ct_id in (select cuc_classtime_id from otter.tb_curriculum);


call retl_table_init("changzhou_xxgl",'tb_curriculum','cuc_id');
call retl_table_init("otter",'tb_class','cla_id');
call retl_table_init("otter",'tb_class_regist_count','crc_id');
call retl_table_init("otter",'tb_department','dept_id');
call retl_table_init("otter",'tb_classtime_type','ctt_id');
call retl_table_init("otter",'tb_classlevel','lev_id');
call retl_table_init("otter",'tb_classtime','ct_id');


call retl_table_init("otter",'tb_system_param','sp_id');
call retl_table_init("otter",'tb_regist','reg_id');
call retl_table_init("otter",'tb_system_param','sp_id');
call retl_table_init("otter",'tb_change_course_amount','cca_id');
call retl_table_init("otter",'tb_teacher','tea_id');
call retl_table_init("otter",'tb_tutor','tutor_id');



# //////////////////////////

insert into `udip_retl`.`retl_buffer` ( `GMT_CREATE`, `PK_DATA`, `TABLE_ID`, `TYPE`, `FULL_NAME`, `GMT_MODIFIED`) values ( '2018-10-17 18:07:09', 'ff808081627322c9016283f19bec64db', '0', 'I', 'changzhou_xxgl.tb_curriculum', '2018-10-17 18:07:09');
