




select r.* from  beijing_xxgl.tb_regist r ,  beijing_xxgl.tb_class c where c.cla_id = r.reg_class_id limit 1000 ;
create table otter.tb_regist_bak select * from otter.tb_regist
create table otter.tb_class_bak select * from otter.tb_class

truncate table otter.tb_class ;
truncate table otter.tb_regist ;


create table otter.tb_regist_1 select r.* from  beijing_xxgl.tb_regist r ,  beijing_xxgl.tb_class c where c.cla_id = r.reg_class_id limit 1000 ;


insert into otter.tb_class


