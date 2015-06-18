drop table  if exists app_test;
create table app_test (
    id int AUTO_INCREMENT,
    cnt varchar(255) ,
    deleted char(1),
    primary key (id)
);

drop table  if exists app_test_0;
create table app_test_0 (
    id int AUTO_INCREMENT,
    cnt varchar(255) ,
    deleted char(1),
    primary key (id)
);

drop table  if exists app_test_table_1;
create table app_test_table_1 (
    id int AUTO_INCREMENT,
    cnt varchar(255) ,
    deleted char(1),
    primary key (id)
);

--DROP SEQUENCE SEQ_APP_TEST_ID if exists;
--CREATE SEQUENCE SEQ_APP_TEST_ID;
