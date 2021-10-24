#/bin/bash
#updated by tsong
#Function export user privileges
#5.7存在问题: show grants for 不会给出密码信息，必须用 show create user
# https://dev.mysql.com/doc/refman/5.7/en/show-grants.html  

# show create user 为5.7版本开始存在，5.6执行报错。

source /etc/profile

pwd=root
expgrants()  
{  
  mysql -B -u'root' -p${pwd} -N  -P3306  $@ -e "SELECT CONCAT(  'SHOW CREATE USER   ''', user, '''@''', host, ''';' ) AS query FROM mysql.user" | \
  mysql -u'root' -p${pwd} -P3306 -f  $@ | \
  sed 's#$#;#g;s/^\(CREATE USER for .*\)/-- \1 /;/--/{x;p;x;}' 
 
  mysql -B -u'root' -p${pwd} -N  -P3306  $@ -e "SELECT CONCAT(  'SHOW GRANTS FOR ''', user, '''@''', host, ''';' ) AS query FROM mysql.user" | \
  mysql -u'root' -p${pwd} -P3306 -f  $@ | \
  sed 's/\(GRANT .*\)/\1;/;s/^\(Grants for .*\)/-- \1 /;/--/{x;p;x;}'   
}  

expgrants > ./5.7_grants.sql
