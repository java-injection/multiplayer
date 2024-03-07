create database logger;
use logger;
show databases;


create table logger(
	id INT AUTO_INCREMENT PRIMARY KEY,
    livello ENUM('INFO', 'WARNING', 'ERROR',"FATAL", 'DEBUG'),
    message VARCHAR(255),
    timestamp_ datetime,
    user_ varchar(255)
);
select * from logger;
alter table logger
add column timestamp_ datetime,
add column user_ varchar(255);