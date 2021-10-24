#!/bin/bash

##备份jenkins脚本(连接远程jenkins服务器，并拉取备份数据目录)

. /etc/profile
. ~/.bash_profile

DATE=$(date '+%Y%m%d')

#JKS_DEST_USER=

#JKS_DEST_PWD=

#JKS_DEST_IP=

#JKS_DATA_BASE_DEST_HOME=

#JKS_DATA_DEST_HOME=

#JKS_BASE_BACKUP_DIR=

#备份过期时间（n-2 天）
#JKS_BACK_UP_EXPIRE_DAYS=22

JKS_BACKUP_DIR=${JKS_BASE_BACKUP_DIR}/${DATE}

JKS_BACKUP_LOG_FILE=${JKS_BASE_BACKUP_DIR}/${DATE}-backup.log



SHELL_OPT_DIR=/opt

${SHELL_OPT_DIR}/auto_ssh_.sh    ${JKS_DEST_USER}    ${JKS_DEST_PWD}     ${JKS_DEST_IP}  


cat <<- EOF | tee /tmp/jks_data-backup.script


if [ -d  ${JKS_DATA_DEST_HOME} ]; then

echo success

exit 0

else

echo fail

exit 1

fi

EOF

if [ ! -d  ${JKS_BASE_BACKUP_DIR} ]; then

mkdir -p  ${JKS_BASE_BACKUP_DIR}

fi

echo $(date)" beging backup ... ">> ${JKS_BACKUP_LOG_FILE}

ssh -Tq -o StrictHostKeyChecking=no  ${JKS_DEST_USER}@${JKS_DEST_IP} < /tmp/jks_data-backup.script

if [ $? -ne 0 ]; then

echo $(date)" backup fail because dest machine dir not exist ">> ${JKS_BACKUP_LOG_FILE}

else

cat <<- EOF | tee /tmp/jks_data-backup.script

cd  ${JKS_DATA_BASE_DEST_HOME}  &&  tar -zcPf  ${JKS_DATA_BASE_DEST_HOME}/jks-data-${DATE}.tar.gz --exclude=${JKS_DATA_DEST_HOME}/workspace  ${JKS_DATA_DEST_HOME}

EOF

ssh -Tq -o StrictHostKeyChecking=no  ${JKS_DEST_USER}@${JKS_DEST_IP} < /tmp/jks_data-backup.script

if [ $? -ne 0 ];then

echo $(date)" backup dump tar fail ...">> ${JKS_BACKUP_LOG_FILE}

else

echo $(date)" backup dump tar success ...">> ${JKS_BACKUP_LOG_FILE}


if [ ! -d  ${JKS_BACKUP_DIR} ]; then

mkdir -p  ${JKS_BACKUP_DIR}

fi

scp -r -P 22  -C ${JKS_DEST_USER}@${JKS_DEST_IP}:${JKS_DATA_BASE_DEST_HOME}/git-data-${DATE}.tar.gz  ${JKS_BACKUP_DIR}

if [ $? -ne 0 ];then

echo $(date)" backup dump  fail ...">> ${JKS_BACKUP_LOG_FILE}

else

find ${JKS_BASE_BACKUP_DIR} -name "20*" -type d -mtime +${JKS_BACK_UP_EXPIRE_DAYS}|tee -a /${JKS_BACKUP_DIR}/del.txt | xargs rm -r 2>>/${JKS_BACKUP_DIR}/error.txt

echo $(date)" backup dump  success ...">> ${JKS_BACKUP_LOG_FILE}

fi

fi

fi

exit 0
