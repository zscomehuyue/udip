create table otter.tb_curriculum  as select * from tb_curriculum limit 1,10000;

/**drop table otter.tb_curriculum ; **/

insert IGNORE into otter.tb_curriculum  select * from tb_curriculum limit 1,10000;

select count(1) from otter.tb_curriculum;

select count(1)  from otter.tb_class ;

 truncate table otter.tb_class ;
create table otter.tb_class as (select * from tb_class cc where exists  (select bb.cuc_class_id from otter.tb_curriculum bb where cc.cla_id = bb.cuc_class_id ) );

insert IGNORE into  otter.tb_class  (select * from tb_class cc where exists  (select bb.cuc_class_id from otter.tb_curriculum bb where cc.cla_id = bb.cuc_class_id ) );


select count(1)  from otter.tb_class ;



 truncate table otter.tb_class_regist_count ;
create table otter.tb_class_regist_count  (select * from tb_class_regist_count cc where exists (select bb.cuc_class_id from otter.tb_curriculum bb where bb.cuc_class_id = cc.crc_class_id))

insert IGNORE into  otter.tb_class_regist_count  (select * from tb_class_regist_count cc where exists (select bb.cuc_class_id from otter.tb_curriculum bb where bb.cuc_class_id = cc.crc_class_id))


select count(1) from otter.tb_class_regist_count ;


truncate table otter.tb_department ;
create table otter.tb_department  (select * from tb_department cc where exists (select bb.cla_servicecenter_id from otter.tb_class bb where bb.cla_servicecenter_id = cc.dept_id))

insert IGNORE into  otter.tb_department  (select * from tb_department cc where exists (select bb.cla_servicecenter_id from otter.tb_class bb where bb.cla_servicecenter_id = cc.dept_id))


select count(1) from otter.tb_department ;



truncate table otter.tb_classlevel ;
create table otter.tb_classlevel (select * from tb_classlevel cc where exists (select bb.cla_level_id from otter.tb_class bb where bb.cla_level_id  = cc.lev_id))

insert IGNORE into  otter.tb_classlevel (select * from tb_classlevel cc where exists (select bb.cla_level_id from otter.tb_class bb where bb.cla_level_id  = cc.lev_id))


select count(1) from otter.tb_classlevel ;





truncate table otter.classtime_type ;
create table otter.tb_classtime_type (select * from tb_classtime_type cc where exists (select bb.cuc_classtimeType_id from otter.tb_curriculum bb where bb.cuc_classtimeType_id  = cc.ctt_id))

insert IGNORE into  otter.tb_classtime_type (select * from tb_classtime_type cc where exists (select bb.cuc_classtimeType_id from otter.tb_curriculum bb where bb.cuc_classtimeType_id  = cc.ctt_id))


select count(1) from otter.tb_classtime_type ;





drop table otter.classtime_type ;


select * from tb_class ;

desc tb_classtime_type;


call retl_table_init("otter",'tb_curriculum','cuc_id');
call retl_table_init("otter",'tb_class','cla_id');
call retl_table_init("otter",'tb_class_regist_count','crc_id');
call retl_table_init("otter",'tb_department','dept_id');
call retl_table_init("otter",'tb_classtime_type','ctt_id');
call retl_table_init("otter",'tb_classlevel','lev_id');

select count(1) from retl.retl_buffer where full_name='otter.tb_class' limit 10  ;
select count(1) from retl.retl_buffer where full_name='otter.tb_classlevel' limit 10  ;


SELECT
 *
FROM
 tb_class xesclass0_
 LEFT JOIN tb_curriculum curriculum1_ ON xesclass0_.cla_id = curriculum1_.cuc_class_id
 LEFT JOIN tb_classtime_type classtimet5_ ON curriculum1_.cuc_classtimeType_id = classtimet5_.ctt_id
 LEFT JOIN tb_classlevel classlevel2_ ON xesclass0_.cla_level_id = classlevel2_.lev_id
 LEFT JOIN tb_class_regist_count registcoun8_ ON xesclass0_.cla_id = registcoun8_.crc_class_id
 LEFT JOIN tb_department servicecen9_ ON xesclass0_.cla_servicecenter_id = servicecen9_.dept_id
WHERE
 1 = 1 limit 10 ;


018-07-26 11:13:08.170 [pipelineId = 2,taskName = ExtractWorker] WARN
c.a.otter.node.etl.extract.extractor.FreedomExtractor - process freedom data error
EventData[tableId=13,tableName=tb_department,schemaName=otter,eventType=
INSERT,executeTime=1532574785000,oldKeys=[],keys=[EventColumn[index=0,columnType=-5,columnName=ID
,columnValue=41133379,isNull=false,isKey=true,isUpdate=true]],columns=[EventColumn[index=1,columnType=4,columnName=TABLE_ID,columnValue=0,isNull=false,isKey=false,isUpdate=true], EventColumn[index=2,columnType=12,columnName=FULL_NAME,columnValue=otter.tb_department,isNull=false,isKey=false,isUpdate=true], EventColumn[index=3,columnType=1,columnName=TYPE,columnValue=I,isNull=false,isKey=false,isUpdate=true], EventColumn[index=4,columnType=12,columnName=PK_DATA,columnValue=ff8080815fba2ae6015fc8f269ff1a8d,isNull=false,isKey=false,isUpdate=true], EventColumn[index=5,columnType=93,columnName=GMT_CREATE,columnValue=2018-07-26 11:13:05,isNull=false,isKey=false,isUpdate=true], EventColumn[index=6,columnType=93,columnName=GMT_MODIFIED,columnValue=2018-07-26 11:13:05,isNull=false,isKey=false,isUpdate=true]],size=80,pairId=-1,sql=<null>,ddlSchemaName=<null>,syncMode=<null>,syncConsistency=<null>,remedy=false,hint=<null>,withoutSchema=false]
com.alibaba.otter.node.etl.extract.exceptions.ExtractException: data pk column size not match , data:EventData[tableId=13,tableName=tb_department,schemaName=otter,eventType=INSERT,executeTime=1532574785000,oldKeys=[],keys=[EventColumn[index=0,columnType=-5,columnName=ID,columnValue=41133379,isNull=false,isKey=true,isUpdate=true]],columns=[EventColumn[index=1,columnType=4,columnName=TABLE_ID,columnValue=0,isNull=false,isKey=false,isUpdate=true], EventColumn[index=2,columnType=12,columnName=FULL_NAME,columnValue=otter.tb_department,isNull=false,isKey=false,isUpdate=true], EventColumn[index=3,columnType=1,columnName=TYPE,columnValue=I,isNull=false,isKey=false,isUpdate=true], EventColumn[index=4,columnType=12,columnName=PK_DATA,columnValue=ff8080815fba2ae6015fc8f269ff1a8d,isNull=false,isKey=false,isUpdate=true], EventColumn[index=5,columnType=93,columnName=GMT_CREATE,columnValue=2018-07-26 11:13:05,isNull=false,isKey=false,isUpdate=true], EventColumn[index=6,columnType=93,columnName=GMT_MODIFIED,columnValue=2018-07-26 11:13:05,isNull=false,isKey=false,isUpdate=true]],size=80,pairId=-1,sql=<null>,ddlSchemaName=<null>,syncMode=<null>,syncConsistency=<null>,remedy=false,hint=<null>,withoutSchema=false]
