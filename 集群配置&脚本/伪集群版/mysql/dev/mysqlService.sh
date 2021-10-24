#! /bin/bash

# chkconfig:   2345 90 10

# description:  mysqlService

mysqld_safe --defaults-extra-file=${MYSQL_HOME}/etc/my.cnf  --user mysql  --log-error=${MYSQL_HOME}/run/mysqld.log   --pid-file=${MYSQL_HOME}/run/mysql.pid

