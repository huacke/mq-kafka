#!/bin/bash

##备份数据库脚本(连接远程mysql服务器，并拉取备份数据目录)


. /etc/profile
. ~/.bash_profile

DATE=$(date '+%Y%m%d')

#GIT_DEST_USER=

#GIT_DEST_PWD=

#GIT_DEST_IP=

#GIT_DATA_BASE_DEST_HOME=

#GIT_DATA_DEST_HOME=

#GIT_BASE_BACKUP_DIR=

#备份过期时间（n-2 天）
#GIT_BACK_UP_EXPIRE_DAYS=22

GIT_BACKUP_DIR=${GIT_BASE_BACKUP_DIR}/${DATE}

GIT_BACKUP_LOG_FILE=${GIT_BASE_BACKUP_DIR}/${DATE}-backup.log

SHELL_OPT_DIR=/opt

${SHELL_OPT_DIR}/auto_ssh_.sh ${GIT_DEST_USER} ${GIT_DEST_PWD} ${GIT_DEST_IP}  


cat <<- EOF | tee /tmp/git_data-backup.script


if [ -d  ${GIT_DATA_DEST_HOME} ]; then

echo success

exit 0

else

echo fail

exit 1

fi

EOF

if [ ! -d  ${GIT_BASE_BACKUP_DIR} ]; then

mkdir -p  ${GIT_BASE_BACKUP_DIR}

fi

echo $(date)" beging backup ... ">> ${GIT_BACKUP_LOG_FILE}

ssh -Tq -o StrictHostKeyChecking=no  ${GIT_DEST_USER}@${GIT_DEST_IP} < /tmp/git_data-backup.script

if [ $? -ne 0 ]; then

echo $(date)" backup fail because dest machine dir not exist ">> ${GIT_BACKUP_LOG_FILE}

else

cat <<- EOF | tee /tmp/git_data-backup.script

cd  ${GIT_DATA_BASE_DEST_HOME}  &&  tar -zcPf  ${GIT_DATA_BASE_DEST_HOME}/git-data-${DATE}.tar.gz ${GIT_DATA_DEST_HOME}

EOF

ssh -Tq -o StrictHostKeyChecking=no ${GIT_DEST_USER}@${GIT_DEST_IP} < /tmp/git_data-backup.script

if [ $? -ne 0 ];then

echo $(date)" backup dump tar fail ...">> ${GIT_BACKUP_LOG_FILE}

else

echo $(date)" backup dump tar success ...">> ${GIT_BACKUP_LOG_FILE}


if [ ! -d  ${GIT_BACKUP_DIR} ]; then

mkdir -p  ${GIT_BACKUP_DIR}

fi

scp  -r -P 22   -C ${GIT_DEST_USER}@${GIT_DEST_IP}:${GIT_DATA_BASE_DEST_HOME}/git-data-${DATE}.tar.gz  ${GIT_BACKUP_DIR}

if [ $? -ne 0 ];then

echo $(date)" backup dump  fail ...">> ${GIT_BACKUP_LOG_FILE}

else

find ${GIT_BASE_BACKUP_DIR} -name "20*" -type d -mtime +${GIT_BACK_UP_EXPIRE_DAYS}|tee -a /${GIT_BACKUP_DIR}/del.txt | xargs rm -r 2>>/${GIT_BACKUP_DIR}/error.txt

echo $(date)" backup dump  success ...">> ${GIT_BACKUP_LOG_FILE}

fi

fi

fi

exit 0
