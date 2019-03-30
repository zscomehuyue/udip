# 全量同步
# 第一步：配置要全量同步的数据通道
# 第二步：在源库中创建[retl]库及相关表结构
# 第三步：运行[retl_table_init]存储过程。



# 创建全量数据同步存储过程
# dbName:数据库名称
# tableName:表名称
# idName:表主键字段名称
drop procedure if EXISTS `retl_table_init`;

DELIMITER $$
create PROCEDURE retl_table_init(dbName VARCHAR(50),tableName VARCHAR(50),idName VARCHAR(20))
begin 

DECLARE i int(11);
DECLARE limit0 int(11);
DECLARE limit1 int(11);
DECLARE num int(11);

set i = 0;
set num = 100;
set limit0 = 0;
set limit1 = num;

set @sqlTextCount = CONCAT("select count(1) into @ct from ",tableName);
prepare stmt from @sqlTextCount; execute stmt; DEALLOCATE PREPARE stmt;



WHILE limit0 < @ct DO

set @sqlText= CONCAT("insert into retl.retl_buffer( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME,GMT_MODIFIED)
		SELECT NOW()",",",idName,",",0,",","'","I","'",",","'",dbName,".",tableName,"'",",","NOW() from ",tableName," limit ",limit0,',',limit1);

prepare stmt from @sqltext; execute stmt; DEALLOCATE PREPARE stmt;

set limit0 = limit0 + num;

end WHILE;

end$$
DELIMITER ;




# 创建全量数据同步存储过程
# dbName:数据库名称
# tableName:表名称
# idName:表主键字段名称
# conditon 查询条件，从where开始
drop procedure if EXISTS `retl_table_init_condition`;

DELIMITER $$
create PROCEDURE retl_table_init_condition(dbName VARCHAR(50),tableName VARCHAR(50),idName VARCHAR(20),conditon VARCHAR(100))
begin 



set @sqlText= CONCAT("insert into retl.retl_buffer( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME,GMT_MODIFIED)
		SELECT NOW()",",",idName,",",0,",","'","I","'",",","'",dbName,".",tableName,"'",",","NOW() from ",tableName,conditon);

prepare stmt from @sqltext; execute stmt; DEALLOCATE PREPARE stmt;



end$$
DELIMITER ;






# 16表分到总的入参 
#call retl_table_init("beijing_xxgl",'tb_class','cla_id');
#call retl_table_init("beijing_xxgl",'tb_curriculum','cuc_id');
#call retl_table_init("beijing_xxgl",'tb_change_class_audit_log','ccal_id');
#call retl_table_init("beijing_xxgl",'tb_class_change','tcc_id');
#call retl_table_init("beijing_xxgl",'tb_class_change_to_audit','ccta_id');
#call retl_table_init("beijing_xxgl",'tb_class_lock_log','cll_id');
#call retl_table_init("beijing_xxgl",'tb_class_modify_log','cml_id');
#call retl_table_init("beijing_xxgl",'tb_classExcel_log','cel_id');
#call retl_table_init("beijing_xxgl",'tb_open_class_audit_log','ocal_id');
#call retl_table_init("beijing_xxgl",'tb_teacherClassExcel_log','cel_id');

# 关系表
#call retl_table_init("beijing_xxgl",'tb_class_classtime','cc_id');
#call retl_table_init("beijing_xxgl",'tb_class_teacher','ct_id');
#call retl_table_init("beijing_xxgl",'tb_class_subject','cs_id');
#call retl_table_init("beijing_xxgl",'tb_cla_modifylog_subject','ms_id');
#call retl_table_init("beijing_xxgl",'tb_cla_modifylog_teacher','ml_id');
#call retl_table_init("beijing_xxgl",'tb_cla_modifylog_classtime','mc_id');


# 查看retl_buffer 表条数
#select count(1) from retl_buffer;

# 删除 retl_buffer 表
#TRUNCATE retl_buffer





drop procedure if EXISTS `retl_table`;

DELIMITER $$
create PROCEDURE retl_table(dbName VARCHAR(50),tableName VARCHAR(50),idName VARCHAR(20),min int,max int)
  begin

    DECLARE i int(11);
    DECLARE limit0 int(11);
    DECLARE limit1 int(11);
    DECLARE num int(11);

    set i = 0;
    set num = 100;
    set limit0 = min;
    set limit1 = max;


    WHILE limit0 < max DO

      set @sqlText= CONCAT("insert into retl.retl_buffer( GMT_CREATE, PK_DATA, TABLE_ID, TYPE, FULL_NAME,GMT_MODIFIED)
		SELECT NOW()",",",idName,",",0,",","'","I","'",",","'",dbName,".",tableName,"'",",","NOW() from ",tableName," limit ",limit0,',',limit1);

      prepare stmt from @sqltext; execute stmt; DEALLOCATE PREPARE stmt;

      set limit0 = limit0 + num;

    end WHILE;

  end$$
DELIMITER ;







