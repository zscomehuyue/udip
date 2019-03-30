-- ☐ 涉及到的表：class  tb_grade_type（全部字段）tb_grade（全部字段） tb_subject（全部字段） tb_classlevel （全部字段）




DROP TABLE IF EXISTS classlevel;
CREATE TABLE classlevel (
  id                   VARCHAR(32)  NOT NULL,
  name                 VARCHAR(60)           DEFAULT NULL,
  city_id              VARCHAR(10)      not NULL DEFAULT '0',
  order                INT(11)               DEFAULT '0',
  display_name         VARCHAR(60)           DEFAULT NULL,
  degree               DOUBLE(4, 2) NOT NULL,
  parent_id            VARCHAR(32)           DEFAULT '0',
  type                 INT(11)               DEFAULT '0',
  deleted              INT(11)               DEFAULT '0',
  create_date          TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP,
  creater_id           VARCHAR(32)           DEFAULT NULL,
  modify_date          TIMESTAMP    NULL     DEFAULT NULL,
  modify_id            VARCHAR(32)           DEFAULT NULL,
  status               INT(11)               DEFAULT '0',
  is_leaf              INT(11)      NOT NULL DEFAULT '1',
  term_is_show_student INT(11)               DEFAULT '0',
  term_is_show_teacher INT(11)               DEFAULT '0',
  is_mobi              INT(4)                DEFAULT '0'  COMMENT '屏蔽摩比数据',
  is_live_lesson       INT(11)               DEFAULT '0',
  disable              INT(11)               DEFAULT '0'
  COMMENT '状态：0-可用，1-停用',
  PRIMARY KEY (id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS grade_type;
CREATE TABLE grade_type (
  id varchar(32) NOT NULL,
  city_id              VARCHAR(10)      not NULL DEFAULT '0',
  name varchar(20) DEFAULT NULL,
  order int(11) DEFAULT NULL,
  create_date timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  isdeleted int(11) DEFAULT '0',
  modify_date timestamp NULL DEFAULT NULL,
  status int(11) DEFAULT '0',
  creater_id varchar(32) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS grade;
CREATE TABLE grade (
  id varchar(32) NOT NULL,
  name varchar(50) DEFAULT NULL,
  city_id              VARCHAR(10)      not NULL DEFAULT '0',
  order int(11) DEFAULT NULL,
  digits int(11) DEFAULT NULL,
  type_id varchar(32) DEFAULT NULL,
  isdeleted int(11) DEFAULT '0',
  status int(11) DEFAULT '0',
  create_date timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  creater_id varchar(32) DEFAULT NULL,
  modify_id varchar(32) DEFAULT NULL,
  modify_date timestamp NULL DEFAULT NULL,
  fullclass_showstatus int(11) DEFAULT '0' COMMENT '是否满班显示(0:不显示 ,1：显示)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS subject;
CREATE TABLE subject (
  id varchar(32) NOT NULL,
  name varchar(30) DEFAULT NULL,
  city_id              VARCHAR(10)      not NULL DEFAULT '0',
  order int(11) DEFAULT NULL,
  type int(11) DEFAULT NULL,
  is_show_tea int(11) DEFAULT '0',
  is_show_stu int(11) DEFAULT '0',
  isdeleted int(11) DEFAULT '0',
  status int(11) DEFAULT '0',
  create_date timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  creater_id varchar(32) DEFAULT NULL,
  modify_id varchar(32) DEFAULT NULL,
  modify_date timestamp NULL DEFAULT NULL,
  allow_samesubject int(11) NOT NULL DEFAULT '0',
  is_show_ser int(11) DEFAULT '0',
  long_value bigint(20) DEFAULT NULL,
  is_live_lesson int(11) DEFAULT '0',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS class_subject;
CREATE TABLE class_subject (
  cs_id varchar(32) NOT NULL DEFAULT '' COMMENT '主键ID',
  cs_class_id varchar(32) DEFAULT NULL,
  cs_subject_id varchar(32) DEFAULT NULL,
  cs_modify_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (cs_id),
  KEY index_cs_class_id (cs_class_id),
  KEY index_cs_subject_id (cs_subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


create table otter.tb_grade_type as select * from beijing_xxgl.tb_grade_type ;
create table otter.tb_grade as select * from beijing_xxgl.tb_grade ;
create table otter.tb_subject as select * from beijing_xxgl.tb_subject ;

call retl_table_init("otter",'tb_class','cla_id');
call retl_table_init("otter",'tb_grade_type','gt_id');
call retl_table_init("otter",'tb_grade','grd_id');
call retl_table_init("otter",'tb_classlevel','lev_id');
call retl_table_init("otter",'tb_subject','subj_id');


