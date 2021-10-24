#!/bin/bash
# chkconfig:   2345 90 10

##启动脚本

. /etc/profile
. ~/.bash_profile

export NACOS_SERVERS="${NODE1_IP}:${NODE1_PORT} ${NODE2_IP}:${NODE2_PORT} ${NODE3_IP}:${NODE3_PORT}"
export CLUSTER_CONF=${BASE_NACOS_HOME}/conf/cluster.conf

${BASE_NACOS_HOME}/bin/config.sh
${BASE_NACOS_HOME}/bin/startSrv.sh ${NODE1_IP}  ${BASE_NACOS_HOME}/node1 &
${BASE_NACOS_HOME}/bin/startSrv.sh ${NODE2_IP}  ${BASE_NACOS_HOME}/node2 &
${BASE_NACOS_HOME}/bin/startSrv.sh ${NODE3_IP}  ${BASE_NACOS_HOME}/node3 &

tail -fn 500 ${BASE_NACOS_HOME}/logs/start.out

