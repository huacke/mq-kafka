
mysql下载地址： https://dev.mysql.com//Downloads/MySQL-5.7/mysql-5.7.21-linux-glibc2.12-x86_64.tar.gz

下载完毕后放到dockerfile目录 

docker-compose build && docker-compose  up -d


1,更改mysql root用户密码

docker exec  -it  mysql-master  /usr/local/mysql/bin/mysql -u root -p

mysql -p
 
use mysql;
 
update mysql.user set authentication_string=password('root') where user='root' and Host = 'localhost';

进入dockerfile目录

docker-compose restart

2,进入宿主机dockerData mysql etc目录,更改配置文件，禁用skip-grant-tables参数

docker exec  -it  mysql-master  sed -i 's/skip-grant-tables=1/#skip-grant-tables=1/g' /usr/local/mysql/etc/my.cnf

3, 进入dockerfile目录 docker-compose restart


4,给远程连接mysql root用户赋予权限

docker exec  -it  mysql-master  /usr/local/mysql/bin/mysql -u root -proot

alter user user() identified by "root";

GRANT ALL PRIVILEGES ON *.* TO root@"%" IDENTIFIED BY 'root' WITH GRANT OPTION;

flush privileges;

//进入容器(交互模式)
docker exec  -it mysql-master /bin/bash



//数据备份/恢复相关



https://www.cnblogs.com/linuxk/p/9371475.html

mysqldump的关键参数

复制代码
-B：指定多个库，在备份文件中增加建库语句和use语句
--compact：去掉备份文件中的注释，适合调试，生产场景不用
-A：备份所有库
-F：刷新binlog日志
--master-data：在备份文件中增加binlog日志文件名及对应的位置点
-x  --lock-all-tables：锁表
-l：只读锁表
-d：只备份表结构
-t：只备份数据
--single-transaction：适合innodb事务数据库的备份
InnoDB表在备份时，通常启用选项--single-transaction来保证备份的一致性，原理是设定本次会话的隔离级别为Repeatable read，来保证本次会话（也就是dump）时，不会看到其它会话已经提交了的数据。
   
   
   
 1 -A --all-databases：导出全部数据库
 2 -Y --all-tablespaces：导出全部表空间
 3 -y --no-tablespaces：不导出任何表空间信息
 4 --add-drop-database每个数据库创建之前添加drop数据库语句。
 5 --add-drop-table每个数据表创建之前添加drop数据表语句。(默认为打开状态，使用--skip-add-drop-table取消选项)
 6 --add-locks在每个表导出之前增加LOCK TABLES并且之后UNLOCK TABLE。(默认为打开状态，使用--skip-add-locks取消选项)
 7 --comments附加注释信息。默认为打开，可以用--skip-comments取消
 8 --compact导出更少的输出信息(用于调试)。去掉注释和头尾等结构。可以使用选项：--skip-add-drop-table --skip-add-locks --skip-comments --skip-disable-keys
 9 -c --complete-insert：使用完整的insert语句(包含列名称)。这么做能提高插入效率，但是可能会受到max_allowed_packet参数的影响而导致插入失败。
10 -C --compress：在客户端和服务器之间启用压缩传递所有信息
11 -B--databases：导出几个数据库。参数后面所有名字参量都被看作数据库名。
12 --debug输出debug信息，用于调试。默认值为：d:t:o,/tmp/
13 --debug-info输出调试信息并退出
14 --default-character-set设置默认字符集，默认值为utf8
15 --delayed-insert采用延时插入方式（INSERT DELAYED）导出数据
16 -E--events：导出事件。
17 --master-data：在备份文件中写入备份时的binlog文件，在恢复进，增量数据从这个文件之后的日志开始恢复。值为1时，binlog文件名和位置没有注释，为2时，则在备份文件中将binlog的文件名和位置进行注释
18 --flush-logs开始导出之前刷新日志。请注意：假如一次导出多个数据库(使用选项--databases或者--all-databases)，将会逐个数据库刷新日志。除使用--lock-all-tables或者--master-data外。在这种情况下，日志将会被刷新一次，相应的所以表同时被锁定。因此，如果打算同时导出和刷新日志应该使用--lock-all-tables 或者--master-data 和--flush-logs。
19 --flush-privileges在导出mysql数据库之后，发出一条FLUSH PRIVILEGES 语句。为了正确恢复，该选项应该用于导出mysql数据库和依赖mysql数据库数据的任何时候。
20 --force在导出过程中忽略出现的SQL错误。
21 -h --host：需要导出的主机信息
22 --ignore-table不导出指定表。指定忽略多个表时，需要重复多次，每次一个表。每个表必须同时指定数据库和表名。例如：--ignore-table=database.table1 --ignore-table=database.table2 ……
23 -x --lock-all-tables：提交请求锁定所有数据库中的所有表，以保证数据的一致性。这是一个全局读锁，并且自动关闭--single-transaction 和--lock-tables 选项。
24 -l --lock-tables：开始导出前，锁定所有表。用READ LOCAL锁定表以允许MyISAM表并行插入。对于支持事务的表例如InnoDB和BDB，--single-transaction是一个更好的选择，因为它根本不需要锁定表。请注意当导出多个数据库时，--lock-tables分别为每个数据库锁定表。因此，该选项不能保证导出文件中的表在数据库之间的逻辑一致性。不同数据库表的导出状态可以完全不同。
25 --single-transaction：适合innodb事务数据库的备份。保证备份的一致性，原理是设定本次会话的隔离级别为Repeatable read，来保证本次会话（也就是dump）时，不会看到其它会话已经提交了的数据。
26 -F：刷新binlog，如果binlog打开了，-F参数会在备份时自动刷新binlog进行切换。
27 -n --no-create-db：只导出数据，而不添加CREATE DATABASE 语句。
28 -t --no-create-info：只导出数据，而不添加CREATE TABLE 语句。
29 -d --no-data：不导出任何数据，只导出数据库表结构。
30 -p --password：连接数据库密码
31 -P --port：连接数据库端口号
32 -u --user：指定连接的用户名。 


1）Myisam引擎：
mysqldump -uroot -p123456 -A -B --master-data=1 -x| gzip > /data/all_$(date +%F).sql.gz

（2）InnoDB引擎：
mysqldump -uroot -p123456 -A -B  --master-data=1 --single-transaction > /data/bak.sql

（3）生产环境DBA给出的命令
a、for MyISAM
mysqldump --user=root --all-databases --flush-privileges --lock-all-tables \
--master-data=1 --flush-logs --triggers --routines --events \
--hex-blob > $BACKUP_DIR/full_dump_$BACKUP_TIMESTAMP.sql

b、for InnoDB
mysqldump --user=root --all-databases --flush-privileges --single-transaction \
--master-data=1 --flush-logs --triggers --routines --events \
--hex-blob > $BACKUP_DIR/full_dump_$BACKUP_TIMESTAMP.sql


//常用备份命令
mysql -h 192.168.0.109 -P3306  -uroot -proot -e 'show databases;'|grep -E -w -v "Database|information_schema|mysql|sys|test|performance_schema" |xargs mysqldump -h 192.168.0.109 -P3306 -uroot -proot -B  --single-transaction  --complete-insert   --flush-logs --triggers --routines --events --hex-blob  --databases >data.sql

//数据导入命令
mysql -uroot -proot -S /usr/local/mysql/run/mysql.sock < data.sql





