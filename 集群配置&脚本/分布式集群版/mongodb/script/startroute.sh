#!/bin/bash

##################################################################

#   FileName  startroute.sh
#   Description mongodb route 

#################################################################

MONGODB_BASE_HOME=/usr/local/mongodb

MONGODB_HOME=${MONGODB_BASE_HOME}/mongodb-4.0.10

MONGODB_CONFIG_HOME=${MONGODB_BASE_HOME}/conf

CLUSTERS=$1

DBPORTS=$2

CONF_PORTS=$3

OLD_IFS="$IFS"
IFS=","
tempc=($CLUSTERS)
tempd=($DBPORTS)
tempe=($CONF_PORTS)
IFS="$OLD_IFS"

CLUSTERS=(${tempc[@]})
DBPORTS=(${tempd[@]})
CONF_PORTS=(${tempe[@]})

if ((${#DBPORTS[@]} > 1));then
  echo -e "not support multiple route port!!!!!!!!"
  exit 1

fi

if ((${#CONF_PORTS[@]} > 1));then
  echo -e "not support multiple config port!!!!!!!!"
  exit 1

fi


IP=0.0.0.0

REPLNAME=configs

CONFIGDB="${REPLNAME}/"


for index in ${!CLUSTERS[@]}

do


((routeIndex=index +1));

if ((${#CLUSTERS[@]} == $routeIndex));then

CONFIGDB=$CONFIGDB"${CLUSTERS[$index]}:${CONF_PORTS[0]}"

else

CONFIGDB=$CONFIGDB"${CLUSTERS[$index]}:${CONF_PORTS[0]},"

fi

done


BINDIR=${MONGODB_HOME}/bin

MONGOS=${BINDIR}/mongos


for index in ${!DBPORTS[@]}

do

((routeIndex=index +1));

DBPORT=${DBPORTS[$index]}


HOMEDIR=/data/mongodb/data/route/route${routeIndex}

LOGDIR=${HOMEDIR}/log

if [ ! -d "${HOMEDIR}" ];then
mkdir -p ${HOMEDIR}
fi

if [ ! -d "${LOGDIR}" ];then
mkdir -p ${LOGDIR}
fi

echo -e "start mongodb route PATH: ${HOMEDIR}  port: ${DBPORT}"

nohup  numactl --interleave=all ${MONGOS}  --bind_ip "${IP}" --port "${DBPORT}"  --logpath "${LOGDIR}/route.log" --pidfilepath "${HOMEDIR}/route.pid" --logappend  --configdb="${CONFIGDB}"  --fork  --config "${MONGODB_CONFIG_HOME}/route.conf"	>/dev/null 2>&1 &	  

echo -e ""

done



