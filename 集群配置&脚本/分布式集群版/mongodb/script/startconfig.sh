#!/bin/bash

##################################################################

#   FileName    startconfig.sh
#   Description mongodb config 

#################################################################

DBPORTS=($*)

MONGODB_BASE_HOME=/usr/local/mongodb

MONGODB_HOME=${MONGODB_BASE_HOME}/mongodb-4.0.10

MONGODB_CONFIG_HOME=${MONGODB_BASE_HOME}/conf

IP=0.0.0.0

BINDIR=${MONGODB_HOME}/bin

MONGOD=${BINDIR}/mongod

REPLNAME=configs

for index in ${!DBPORTS[@]}
do

((confIndex=index +1));

DBPORT=${DBPORTS[$index]}

HOMEDIR=/data/mongodb/data/confSrv/confSrv${confIndex} 

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

ARGS="--configsvr --replSet ${REPLNAME} "

echo -e "start mongodb configsrv PATH: ${HOMEDIR}  port: ${DBPORT}"

numactl --interleave=all ${MONGOD}  --bind_ip "${IP}" --port "${DBPORT}" --dbpath ${DATADIR} --logpath "${LOGDIR}/configsrv.log" --journal --pidfilepath "${HOMEDIR}/configsrv.pid" --logappend ${ARGS} --fork  --config "${MONGODB_CONFIG_HOME}/configSrv.conf"		  

echo -e ""

done




