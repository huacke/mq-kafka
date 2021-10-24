#!/bin/bash

# chkconfig:   2345 90 10



##启动脚本



. /etc/profile

. ~/.bash_profile



sh /opt/config.sh 



${REIDS_HOME}/script/startcluster.sh start



touch /var/run.log



tail -fn 500 /var/run.log



