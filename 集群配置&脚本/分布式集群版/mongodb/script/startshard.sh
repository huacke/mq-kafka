#!/bin/bash

##################################################################

#   FileName  startshard.sh
#   Description mongodb shard 

#################################################################

MONGODB_BASE_HOME=/usr/local/mongodb

MONGODB_HOME=${MONGODB_BASE_HOME}/mongodb-4.0.10

MONGODB_CONFIG_HOME=${MONGODB_BASE_HOME}/conf


DBPORTS=$1

SHARD=$2

OLD_IFS="$IFS"
IFS=","
temp=($DBPORTS)
IFS="$OLD_IFS"
DBPORTS=(${temp[@]})

if ((${#DBPORTS[@]} > 1));then
  echo -e "not support multiple shard port!!!!!!!!"
  exit 1
fi


IP=0.0.0.0

BINDIR=${MONGODB_HOME}/bin

MONGOD=${BINDIR}/mongod

for index in ${!DBPORTS[@]}
do

DBPORT=${DBPORTS[$index]}

REPLNAME=shard${SHARD}

HOMEDIR=/data/mongodb/data/shard/shard$SHARD

DATADIR=${HOMEDIR}/db

LOGDIR=${HOMEDIR}/log

if [ ! -d "${HOMEDIR}" ];then
mkdir -p ${HOMEDIR}
fi

if [ ! -d "${DATADIR}" ];then
mkdir -p ${DATADIR}
fi

if [ ! -d "${LOGDIR}" ];then
mkdir -p ${LOGDIR}
fi

ARGS="--shardsvr --replSet ${REPLNAME} "

echo -e "start mongodb shardsrv PATH: ${HOMEDIR}  port: ${DBPORT}"

numactl --interleave=all ${MONGOD}  --bind_ip "${IP}" --port "${DBPORT}" --dbpath ${DATADIR} --logpath "${LOGDIR}/mongod.log" --journal --pidfilepath "${HOMEDIR}/mongod.pid" --logappend ${ARGS} --fork --config "${MONGODB_CONFIG_HOME}/shard.conf"		  

echo -e ""

done



