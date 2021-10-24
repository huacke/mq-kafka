#!/bin/bash

##备份maven仓库脚本(连接远程maven仓库服务器，并拉取备份数据目录)

. /etc/profile
. ~/.bash_profile

DATE=$(date '+%Y%m%d')

#MVN_DEST_USER=root

#MVN_DEST_PWD=

#MVN_DEST_IP=

#MVN_DATA_BASE_DEST_HOME=

#MVN_DATA_DEST_HOME=

#MVN_BASE_BACKUP_DIR=

#备份过期时间（n-2 天）
#MVN_BACK_UP_EXPIRE_DAYS=22

MVN_BACKUP_DIR=${MVN_BASE_BACKUP_DIR}/${DATE}

MVN_BACKUP_LOG_FILE=${MVN_BASE_BACKUP_DIR}/${DATE}-backup.log



SHELL_OPT_DIR=/opt

${SHELL_OPT_DIR}/auto_ssh_.sh    ${MVN_DEST_USER}    ${MVN_DEST_PWD}     ${MVN_DEST_IP}  


cat <<- EOF | tee /tmp/mvn_data-backup.script


if [ -d  ${MVN_DATA_DEST_HOME} ]; then

echo success

exit 0

else

echo fail

exit 1

fi

EOF

if [ ! -d  ${MVN_BASE_BACKUP_DIR} ]; then

mkdir -p  ${MVN_BASE_BACKUP_DIR}

fi



echo $(date)" beging backup ... ">> ${MVN_BACKUP_LOG_FILE}

ssh -Tq -o StrictHostKeyChecking=no  ${MVN_DEST_USER}@${MVN_DEST_IP} < /tmp/mvn_data-backup.script

if [ $? -ne 0 ]; then

echo $(date)" backup fail because dest machine dir not exist ">> ${MVN_BACKUP_LOG_FILE}

else

cat <<- EOF | tee /tmp/mvn_data-backup.script

cd  ${MVN_DATA_BASE_DEST_HOME}  &&  tar -zcPf  ${MVN_DATA_BASE_DEST_HOME}/mvn-data-${DATE}.tar.gz ${MVN_DATA_DEST_HOME}

EOF

ssh -Tq -o StrictHostKeyChecking=no  ${MVN_DEST_USER}@${MVN_DEST_IP} < /tmp/mvn_data-backup.script

if [ $? -ne 0 ];then

echo $(date)" backup dump tar fail ...">> ${MVN_BACKUP_LOG_FILE}

else

echo $(date)" backup dump tar success ...">> ${MVN_BACKUP_LOG_FILE}


if [ ! -d  ${MVN_BACKUP_DIR} ]; then

mkdir -p  ${MVN_BACKUP_DIR}

fi

scp -r -P 22  -C ${MVN_DEST_USER}@${MVN_DEST_IP}:${MVN_DATA_BASE_DEST_HOME}/mvn-data-${DATE}.tar.gz  ${MVN_BACKUP_DIR}

if [ $? -ne 0 ];then

echo $(date)" backup dump  fail ...">> ${MVN_BACKUP_LOG_FILE}

else

find ${MVN_BASE_BACKUP_DIR} -name "20*" -type d -mtime +${MVN_BACK_UP_EXPIRE_DAYS}|tee -a /${MVN_BACKUP_DIR}/del.txt | xargs rm -r 2>>/${MVN_BACKUP_DIR}/error.txt

echo $(date)" backup dump  success ...">> ${MVN_BACKUP_LOG_FILE}

fi

fi

fi

exit 0
