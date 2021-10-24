#!/bin/bash

#set -x

/usr/sbin/crond -i

echo "$(env)" | sed 's/[ ]\{1,\}/\n/g'  >> /etc/profile

source /etc/profile

touch ${CRON_LOG_FILE}

tail -fn 500 /data/backup/cron.log