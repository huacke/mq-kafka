#!/bin/bash

# chkconfig:   2345 90 10

##################################################################
#   FileName startZK.sh
#   Description start zookeeper  service
#################################################################
source /etc/profile

start()
{

ZOOKEEPER_BASE_HOME=/usr/local/zookeeper

ZOOKEEPER_HOME=/usr/local/zookeeper/apache-zookeeper-3.5.5

ZOOKEEPER_BIN_HOME=${ZOOKEEPER_HOME}/bin

ZOOKEEPER_BIN=${ZOOKEEPER_BIN_HOME}/zkServer.sh

ZOOKEEPER_CONFIG_HOME=${ZOOKEEPER_BASE_HOME}/conf

ZOOKEEPER_LOG_PATH=/data/zookeeper/logs

export JMXPORT="19998"

#自定义jvm参数，在zookeeper 安装的bin 目录下的zkEnv.sh 末尾加入 export SERVER_JVMFLAGS="${ZOOKEEPER_JVM_FLAG}" 
export ZOOKEEPER_JVM_FLAG="-Xms3G -Xmx3G -Xmn1G -XX:+AlwaysPreTouch -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -Xloggc:${ZOOKEEPER_LOG_PATH}/zookeeper_`date '+%Y%m%d%H%M%S'`.gc -XX:-UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=64M"

hasconfigJvm=$( cat ${ZOOKEEPER_HOME}/bin/zkEnv.sh | tail -n 5 | grep "ZOOKEEPER_JVM_FLAG=")
if [[ "$hasconfigJvm" == "" ]];then
echo "export ZOOKEEPER_JVM_FLAG=\$ZOOKEEPER_JVM_FLAG" >> ${ZOOKEEPER_BIN_HOME}/zkEnv.sh
echo "export SERVER_JVMFLAGS=\$ZOOKEEPER_JVM_FLAG" >> ${ZOOKEEPER_BIN_HOME}/zkEnv.sh
fi


echo -e "Starting  zookeeper  service    ....."

ZOO_LOG_DIR=${ZOOKEEPER_LOG_PATH}  ${ZOOKEEPER_BIN} start-foreground  ${ZOOKEEPER_CONFIG_HOME}/zoo.cfg >>${ZOOKEEPER_LOG_PATH}/zookeeper.log 

echo -e "Starting zookeeper service  done"


}

stop()

{

PIDS=$(ps ax | grep -i 'zookeeper.server.quorum.QuorumPeerMain' | grep -v grep | awk '{print $1}')

if [ "$PIDS" == "" ]

then

   echo -e "NO such process found!"

   exit 1

fi

echo -e "Stopping zookeeper :{$PIDS} ...."

kill -15 ${PIDS}

exit 0

}

restart()
{
    echo "INFO : zookeeper  service is stoping ... "
    stop
    echo "INFO : zookeeper  service is starting ..."
    start
}


status()

{

  Z_PIDS=$(ps -elf | grep -i 'zookeeper.server.quorum.QuorumPeerMain' |grep -v grep |awk '{print $4}' |xargs )

  if [ "$C_PIDS" == "" ]

  then

 Z_STATUS="NOT running"

  else

 Z_STATUS="Running"

  fi

  echo -e "zookeeper nodes:{$Z_PIDS}:{${Z_STATUS}}"

  exit 0 

}

usage()

{

 echo -e "Usage: $0 [start|restart|stop|status]"

 exit 1

}

case  "$1" in

start)

start

;;

stop)

stop

;;

restart)

restart

;;

status)

status

;;

*)

usage

;;

esac
