#!/bin/bash

##备份数据库脚本(连接远程mysql服务器，并拉取备份数据目录)

. /etc/profile
. ~/.bash_profile

DATE=$(date '+%Y%m%d')

#MYSQL_DB_USER=

#MYSQL_DB_PWD=

#MYSQL_DEST_USER=

#MYSQL_DEST_PWD=

#MYSQL_DEST_IP=

#MYSQL_BASE_BACKUP_DIR=

MYSQL_BACKUP_DIR=${MYSQL_BASE_BACKUP_DIR}/${DATE}

MYSQL_BACKUP_LOG_FILE=${MYSQL_BASE_BACKUP_DIR}/${DATE}-backup.log

SHELL_OPT_DIR=/opt

${SHELL_OPT_DIR}/auto_ssh_.sh    ${MYSQL_DEST_USER}    ${MYSQL_DEST_PWD}     ${MYSQL_DEST_IP}  

#备份过期时间（n-2 天）
#MYSQL_BACK_UP_EXPIRE_DAYS=22


if [ ! -d  ${MYSQL_BASE_BACKUP_DIR} ]; then

mkdir -p  ${MYSQL_BASE_BACKUP_DIR}

fi

touch ${MYSQL_BACKUP_LOG_FILE}

echo " $(date)beging backup ... ">> ${MYSQL_BACKUP_LOG_FILE}

cat <<- EOF | tee /tmp/mysql-backup.script 

sh ${MYSQL_DEST_BACKUP_SHELL}  ${MYSQL_DB_USER} ${MYSQL_DB_PWD}  ${MYSQL_DEST_IP}

EOF

ssh -Tq -o StrictHostKeyChecking=no  ${MYSQL_DEST_USER}@${MYSQL_DEST_IP} < /tmp/mysql-backup.script

if [ $? -ne 0 ]; then

echo "$(date)backup fail because excute dest mysql machine backup shell script   not success  ">> ${MYSQL_BACKUP_LOG_FILE}

else


cat <<- EOF | tee /tmp/mysql-backup.script 

if [ -d  ${MYSQL_BACKUP_DIR} ]; then

echo success

exit 0

else

echo fail

exit 1

fi

EOF

ssh -Tq -o StrictHostKeyChecking=no  ${MYSQL_DEST_USER}@${MYSQL_DEST_IP} < /tmp/mysql-backup.script

if [ $? -ne 0 ]; then

echo "$(date)backup fail because dest machine dir not exist ">> ${MYSQL_BACKUP_LOG_FILE}

else

if [ ! -d  ${MYSQL_BACKUP_DIR} ]; then

mkdir -p  ${MYSQL_BACKUP_DIR}

fi

scp -r -P 22  -C ${MYSQL_DEST_USER}@${MYSQL_DEST_IP}:${MYSQL_BACKUP_DIR}  ${MYSQL_BASE_BACKUP_DIR} 

if [ $? -ne 0 ];then

echo "$(date)backup dump  fail ...">> ${MYSQL_BACKUP_LOG_FILE}

else

find ${MYSQL_BASE_BACKUP_DIR} -name "20*" -type d -mtime +${MYSQL_BACK_UP_EXPIRE_DAYS}|tee -a /${MYSQL_BACKUP_DIR}/del.txt | xargs rm -r 2>>/${MYSQL_BACKUP_DIR}/error.txt

echo "$(date) backup dump  success ...">> ${MYSQL_BACKUP_LOG_FILE}

fi

fi

fi

exit 0
