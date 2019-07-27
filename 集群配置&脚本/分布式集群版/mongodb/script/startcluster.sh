#!/bin/bash
# chkconfig:   2345 90 10
##################################################################

#   FileName startcluster.sh
#   Description mongodb startcluster

#################################################################

start()

{

IP=$(ip addr |grep inet |grep brd |awk -F' ' '{ print $2}'|awk -F'/' '{print $1}')

if [ "$IP" == "" ]

then

  echo -e "Failed to get IP on this host."

  exit 1

fi

echo "never" > /sys/kernel/mm/transparent_hugepage/enabled
echo "never" > /sys/kernel/mm/transparent_hugepage/defrag

MONGODB_BASE_HOME=/usr/local/mongodb

MONGODB_HOME=${MONGODB_BASE_HOME}/mongodb-4.0.10

SHELL_HOME=${MONGODB_BASE_HOME}/script

CLUSTERS="192.168.0.109,192.168.0.111,192.168.0.117"

CONFIG_PORTS="21001"

MMS_PORTS="24001"

MMS_BACKUP_PORTS="25001"

SHARD1_PORTS="22001"

SHARD2_PORTS="22002"

SHARD3_PORTS="22003"

ROUTE_PORTS="23001"

CONFIG_ADDRESSES="${CONFIG_PORTS}"

echo -e "Starting  mongodb  cluster    ....."



#start config args=> prots 
echo -e "Starting config nodes @{$CONFIG_PORTS} ..." 

${SHELL_HOME}/startconfig.sh  $CONFIG_PORTS 

echo -e "Starting shard nodes mms:@{${MMS_PORTS}}...."

#start config args=> prots shardname 
${SHELL_HOME}/startshard.sh    ${MMS_PORTS}  mms

#start config args=> prots shardname 
${SHELL_HOME}/startshard.sh    ${MMS_BACKUP_PORTS}  mms-backup

#start shard  args=> prots shardname
echo -e "Starting shard nodes shard1:@{${SHARD1_PORTS}}...."

${SHELL_HOME}/startshard.sh    ${SHARD1_PORTS} 1

echo -e "Starting shard nodes shard2:@{${SHARD2_PORTS}}...."

${SHELL_HOME}/startshard.sh    ${SHARD2_PORTS} 2

echo -e "Starting shard nodes shard3:@{${SHARD3_PORTS}}...."

${SHELL_HOME}/startshard.sh    ${SHARD3_PORTS} 3


#start route   args=> clusterNodes  routePorts
echo -e "Starting route nodes @{$ROUTE_PORTS} with CONFIG PORTS :{$CONFIG_ADDRESSES}...."

${SHELL_HOME}/startroute.sh    ${CLUSTERS} ${ROUTE_PORTS} ${CONFIG_PORTS}



echo -e "===ALL DONE====="

}

stop()

{

PIDS=$(pidof mongod mongos 2>/dev/null )

if [ "$PIDS" == "" ]

then

   echo -e "NO such process found!"

   exit 1

fi

echo -e "Stopping mongodb and mongos:{$PIDS} ...."

kill -15 ${PIDS}

exit 0

}

restart()
{
    echo "INFO : mongodb cluster  service is stoping ... "
    stop
    echo "INFO : mongodb cluster is starting ..."
    start
}


status()

{

  C_PIDS=$(ps -elf |grep mongod |grep configsvr |grep -v grep |awk '{print $4}' |xargs )

  D_PIDS=$(ps -elf |grep mongod |grep shardsvr |grep -v grep |awk '{print $4}' |xargs )

  R_PIDS=$(ps -elf |grep mongos |grep -v grep |awk '{print $4}' |xargs )

  if [ "$C_PIDS" == "" ]

  then

 C_STATUS="NOT running"

  else

 C_STATUS="Running"

  fi

  if [ "$D_PIDS" == "" ]

  then

 D_STATUS="NOT running"

  else

 D_STATUS="Running"

  fi

  if [ "$R_PIDS" == "" ]

  then

 R_STATUS="NOT running"

  else

 R_STATUS="Running"

  fi

  echo -e "config nodes:{$C_PIDS}:{${C_STATUS}}"

  echo -e "shard nodes :{$D_PIDS}:{${D_STATUS}}"

  echo -e "route nodes :{$R_PIDS}:{${R_STATUS}}"

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
