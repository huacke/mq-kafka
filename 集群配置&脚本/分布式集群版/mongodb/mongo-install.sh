#!/bin/bash


. /etc/profile
. ~/.bash_profile


#当前机器ip
NODE_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | awk -F"/" '{print $1}')

#mongodb安装版本
export MONGO_VERSION=4.0.10

#基础目录
export BASE_INSTALL_DIR=/usr/local

#mongodb安装目录
export MONGO_INSTALL_DIR=${BASE_INSTALL_DIR}/mongo

#mongodb数据目录
export MONGO_DATA_DIR=${MONGO_INSTALL_DIR}/data

#mongodb配置目录
export MONGO_CONF_DIR=${MONGO_INSTALL_DIR}/conf

#mongodb日志目录
export MONGO_LOG_DIR=${MONGO_INSTALL_DIR}/logs

#mongodb配置服务端口
export MONGO_CONF_SRV_PORT=21001;

#mongodb分片服务起始端口
export MONGO_SHARD_SRV_BEGIN_PORT=22001;

#mongodb路由服务端口
export MONGO_ROUT_SRV_PORT=23001;

#mongodb配置服务副本集名称
export MONGO_CONF_REPL_SET_NAME=configs

#mongodb MMS服务副本集名称
export MONGO_SHARD_MMS_REPL_SET_NAME=mms

#mongodb MMS备份服务副本集名称
export MONGO_SHARD_MMS_BACKUP_REPL_SET_NAME=mms-backup

#mongodb MMS 端口
export MONGO_MMS_PORTS=24001

#mongodb MMS备份端口
export MONGO_MMS_BACKUP_PORTS=25001

#mongodb 分片数量
export MONGO_SHARD_NUM=3

#集群节点（根据集群节点数据自动计算，config，shard,route数量，计算公式，总数量： config = route = 集群节点数量，shard= 集群节点数 * MONGO_SHARD_NUM）
#每台机器 conf=route=1  shard=MONGO_SHARD_NUM
export CLUSTER_NODES_IPS=192.168.0.109,192.168.0.111,192.168.0.100



OLD_IFS="$IFS"
IFS=","
temp=($CLUSTER_NODES_IPS)
IFS="$OLD_IFS"
export CLUSTER_NODES=(${temp[@]})

yum install numactl axel -y


#安装mongodb
cd /tmp && rm -rf mongodb-* && axel -a -n 35 https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel70-${MONGO_VERSION}.tgz &&  tar -xzf mongodb-linux-x86_64-rhel70-${MONGO_VERSION}.tgz   -C /tmp && mv /tmp/mongodb-linux-x86_64-rhel70-${MONGO_VERSION} ${MONGO_INSTALL_DIR} &&  rm -rf /tmp/mongo*


mkdir -p ${MONGO_INSTALL_DIR}/{script,conf,data,logs}

mkdir -p ${MONGO_DATA_DIR}/{config-db,route,shard}


#配置服务集群地址
export CONFIG_DB_ADDR="${MONGO_CONF_REPL_SET_NAME}/"

for index in ${!CLUSTER_NODES[@]}

do

if [ "$index" == "$((${#CLUSTER_NODES[@]}-1))" ];then

CONFIG_DB_ADDR=$CONFIG_DB_ADDR${CLUSTER_NODES[$index]}":"${MONGO_CONF_SRV_PORT}

else

CONFIG_DB_ADDR=$CONFIG_DB_ADDR${CLUSTER_NODES[$index]}":"${MONGO_CONF_SRV_PORT}","

fi

done





#生成秘钥文件
#openssl rand -base64 500 > ${MONGO_INSTALL_DIR}/keyfile

#秘钥文件生成
cat <<- EOF | tee ${MONGO_INSTALL_DIR}/keyfile
mxX0rx75o1NiW7xMAJmRI/0oNaLJwL8SX5fg9BI5Op356jFFcrKz4Bm5tCChHvmr
HTAnD1MZp2nBENHutvyaydVrCT07cmvg6iwpAf4xKxvV2PszD5LzjMj+Es426sda
t6FdscXwnW8N3ISLp8EUevBmRGEcwBMOmDpLhSKYElbOjSmL5CrZSkCqm++UWzGM
uGhv7NMFTN5oFDtsn4LDV1VnMy+6j0GzU9sckIJloqPk3sW+Z1xiNZvGw8mvdXhh
g1N/9AaO8WBN4viO8gRDZOtqV7UqoosiiYl2YKj5ccFQTQaxVm2WOR7WlWf/o98f
4/MQDZNph95IS+8vMqprR2J3SDz5dI4W2rpBH4MBtD1//P38smqE08dLC4hcAgek
ST1rP1c1kDpEoBpT1oM4gec8jPQxCe26a2VNAe8ap7iBgN4L0iaMm1l4jkc1dTie
1hvp0HxZztSTLBFo2KGqM088OIQ0qLjXSEk3wYsb76ECDfKcdvg8oZnaT1prZ4I4
FezXkkxTbBR+yeyM2d7UPkGoahL3xC7A+erjN2MByy6UEKks1St/i9huQHA4gCW2
n9M0UgibFDqZErgGwa6DStZSNlAOFLWPeYhgXHS48jLtghIaPDfgKsRetz2KyuYO
WLrxDlc/UHsuV23Xvr6VT5DewDE=
EOF

chmod 400 ${MONGO_INSTALL_DIR}/keyfile

#configSrv配置生成
cat <<- EOF | tee ${MONGO_CONF_DIR}/configSrv.conf

#此配置仅仅作为一个模板，配置，核心参数将在启动脚本重载

systemLog:

    destination: file

    logAppend: true

    path: 

net:

    port: ${MONGO_CONF_SRV_PORT}

    bindIp: 0.0.0.0

processManagement:

    fork: true

    pidFilePath: ${MONGO_INSTALL_DIR}/mongodb-conf.pid

storage:

   dbPath: ${MONGO_DATA_DIR}/config-db

replication:

   replSetName: ${MONGO_CONF_REPL_SET_NAME}

security:

   keyFile: ${MONGO_INSTALL_DIR}/keyfile

   clusterAuthMode: keyFile

sharding:

    clusterRole: configsvr

EOF





#route配置生成
cat <<- EOF | tee ${MONGO_CONF_DIR}/route.conf

#此配置仅仅作为一个模板，配置，核心参数将在启动脚本重载

systemLog:

    destination: file

    logAppend: true

    path: ${MONGO_LOG_DIR}/mongos.log

net:

    port: ${MONGO_ROUT_SRV_PORT}

    bindIp: 0.0.0.0

processManagement:

    fork: true

    pidFilePath: ${MONGO_INSTALL_DIR}/mongos.pid

sharding:

    configDB: ${CONFIG_DB_ADDR}

security:

   keyFile: ${MONGO_INSTALL_DIR}/keyfile

   clusterAuthMode: keyFile

EOF





for index in ${!CLUSTER_NODES[@]}

do

it=$(($index+1))

mkdir -p ${MONGO_DATA_DIR}/shard/shard$it

done

#调整limit参数
echo -e " * hard nofile 65536 \n * soft nofile 65536 \n * hard memlock unlimited \n * hard nproc 2048 \n * hard as unlimited \n"  >>/etc/security/limits.conf
ulimit -Hn


#shard 配置生成
cat <<- EOF | tee ${MONGO_CONF_DIR}/shard.conf

#此配置仅仅作为一个模板，配置，核心参数将在启动脚本重载

systemLog:

    destination: file

    logAppend: true

    path: ${MONGO_LOG_DIR}/db-shard.log

net:

    port: ${MONGO_SHARD_SRV_BEGIN_PORT}

    bindIp: 0.0.0.0

processManagement:

    fork: true

    pidFilePath: ${MONGO_INSTALL_DIR}/mongodb-shard.pid

storage:

   dbPath: ${MONGO_DATA_DIR}/db/shard$it

   journal: 

      enabled: true

      commitIntervalMs: 500

   directoryPerDB: true

   wiredTiger:

      engineConfig:

         cacheSizeGB: 2

replication:

   oplogSizeMB: 10000

   replSetName: shard

operationProfiling:

   mode: slowOp

   slowOpThresholdMs: 1000

security:

   keyFile: ${MONGO_INSTALL_DIR}/keyfile

   clusterAuthMode: keyFile

   authorization: enabled

sharding:

    clusterRole: shardsvr

EOF



#集群启动脚本生成
cat <<- EOF | tee ${MONGO_INSTALL_DIR}/script/startcluster.sh
#!/bin/bash

#mogodb安装目录
MONGODB_HOME=${MONGO_INSTALL_DIR}

#脚本目录
SHELL_HOME=\${MONGODB_HOME}/script

#集群节点ip
CLUSTER_IPS=${CLUSTER_NODES_IPS}

#配置服务端口
CONFIG_PORTS=${MONGO_CONF_SRV_PORT}

#mongodb分片服务起始端口
SHARD_SRV_BEGIN_PORT=${MONGO_SHARD_SRV_BEGIN_PORT}

#路由端口
ROUTE_PORTS=${MONGO_ROUT_SRV_PORT}

#MMS 端口
MMS_PORTS=${MONGO_MMS_PORTS}

#MMS备份端口
MMS_BACKUP_PORTS=${MONGO_MMS_BACKUP_PORTS}

#mongodb MMS服务副本集名称
SHARD_MMS_REPL_SET_NAME=${MONGO_SHARD_MMS_REPL_SET_NAME}

#mongodb MMS备份服务副本集名称
SHARD_MMS_BACKUP_REPL_SET_NAME=${MONGO_SHARD_MMS_BACKUP_REPL_SET_NAME}

#mongodb 分片数量
export SHARD_NUM=${MONGO_SHARD_NUM}

start()
{

IP=\$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print \$2}' | awk -F"/" '{print \$1}')

if [ "\$IP" == "" ];then

  echo -e "Failed to get IP on this host."

  exit 1

fi

echo "never" > /sys/kernel/mm/transparent_hugepage/enabled

echo "never" > /sys/kernel/mm/transparent_hugepage/defrag

sysctl -w vm.max_map_count=262144

sysctl -w vm.swappiness=1

sysctl -w vm.zone_reclaim_mode=0

echo 0 > /proc/sys/vm/zone_reclaim_mode

sysctl -w vm.overcommit_memory=1

sysctl -w net.core.somaxconn=2056

echo -e "Starting  mongodb  cluster    ....."

echo -e "Starting config nodes @{\$CONFIG_PORTS} ..." 

\${SHELL_HOME}/startconfig.sh  \$CONFIG_PORTS 


for index in  \$(seq 0 \$((\${SHARD_NUM}-1)))

do

it=\$((\$index+1))

shardPort=\$((\${SHARD_SRV_BEGIN_PORT}+\$index))

echo -e "Starting shard nodes shard\$it:@{\${shardPort}}...."

\${SHELL_HOME}/startshard.sh    \$shardPort  \$it

done


echo -e "Starting shard nodes mms:@{\${MMS_PORTS}}...."

\${SHELL_HOME}/startshard.sh    \${MMS_PORTS}  \${SHARD_MMS_REPL_SET_NAME}

echo -e "Starting shard nodes mms-backup:@{\${MMS_BACKUP_PORTS}}...."

\${SHELL_HOME}/startshard.sh    \${MMS_BACKUP_PORTS}  \${SHARD_MMS_BACKUP_REPL_SET_NAME}

echo -e "Starting route nodes @{\$ROUTE_PORTS} with CONFIG PORTS :{\$CONFIG_PORTS}...."

\${SHELL_HOME}/startroute.sh  \${CLUSTER_IPS}  \${ROUTE_PORTS} \${CONFIG_PORTS}

echo -e "===ALL DONE====="

}


stop()
{

PIDS=\$(pidof mongod mongos 2>/dev/null )

if [ "\$PIDS" == "" ]; then

   echo -e "NO such process found!"

   exit 1

fi

echo -e "Stopping mongodb and mongos:{\$PIDS} ...."

kill -15  \${PIDS}

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

  C_PIDS=\$(ps -elf |grep mongod |grep configsvr |grep -v grep |awk '{print \$4}' |xargs )

  D_PIDS=\$(ps -elf |grep mongod |grep shardsvr |grep -v grep |awk '{print \$4}' |xargs )

  R_PIDS=\$(ps -elf |grep mongos |grep -v grep |awk '{print \$4}' |xargs )

  if [ "\$C_PIDS" == "" ]

  then

 C_STATUS="NOT running"

  else

 C_STATUS="Running"

  fi

  if [ "\$D_PIDS" == "" ]

  then

 D_STATUS="NOT running"

  else

 D_STATUS="Running"

  fi

  if [ "\$R_PIDS" == "" ]

  then

 R_STATUS="NOT running"

  else

 R_STATUS="Running"

  fi

  echo -e "config nodes:{\$C_PIDS}:{\${C_STATUS}}"

  echo -e "shard nodes :{\$D_PIDS}:{\${D_STATUS}}"

  echo -e "route nodes :{\$R_PIDS}:{\${R_STATUS}}"

  exit 0 

}

usage()
{

 echo -e "Usage: \$0 [start|restart|stop|status]"
 exit 1
}

case  "\$1" in

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

EOF


#配置服务启动脚本生成
cat <<- EOF | tee ${MONGO_INSTALL_DIR}/script/startconfig.sh
#!/bin/bash

DBPORTS=(\$*)

IP=0.0.0.0

MONGODB_HOME=${MONGO_INSTALL_DIR}

MONGODB_CONFIG_HOME=${MONGO_CONF_DIR}

HOMEDIR=${MONGO_DATA_DIR}/config-db

LOGDIR=${MONGO_LOG_DIR}

BINDIR=\${MONGODB_HOME}/bin

MONGOD=\${BINDIR}/mongod

REPLNAME=${MONGO_CONF_REPL_SET_NAME}

for index in \${!DBPORTS[@]}

do

((confIndex=index +1));

DATADIR=\${HOMEDIR}/db\${confIndex}

DBPORT=\${DBPORTS[\$index]}

if [ ! -d "\${HOMEDIR}" ];then

mkdir -p \${HOMEDIR}

fi

if [ ! -d "\${DATADIR}" ];then

mkdir -p \${DATADIR}

fi

if [ ! -d "\${LOGDIR}" ];then

mkdir -p \${LOGDIR}

fi

ARGS="--configsvr --replSet \${REPLNAME} "

echo -e "start mongodb configsrv PATH: \${DATADIR}  port: \${DBPORT}"

numactl --interleave=all \${MONGOD}  --bind_ip "\${IP}" --port "\${DBPORT}" --dbpath \${DATADIR} --logpath "\${LOGDIR}/configsrv.log" --journal --pidfilepath "\${DATADIR}/configsrv.pid" --logappend \${ARGS} --fork  --config "\${MONGODB_CONFIG_HOME}/configSrv.conf"		  

echo -e ""

done

EOF


#路由服务启动脚本生成
cat <<- EOF | tee ${MONGO_INSTALL_DIR}/script/startroute.sh
#!/bin/bash

CLUSTERS=(\$1)

OLD_IFS="\$IFS"
IFS=","
temp=(\$CLUSTERS)
IFS="\$OLD_IFS"
CLUSTERS=(\${temp[@]})

DBPORTS=(\$2)

CONF_PORTS=(\$3)

IP=0.0.0.0

MONGODB_HOME=${MONGO_INSTALL_DIR}

MONGODB_DATA_HOME=${MONGO_DATA_DIR}

MONGODB_CONFIG_HOME=${MONGO_CONF_DIR}

LOGDIR=${MONGO_LOG_DIR}

BINDIR=\${MONGODB_HOME}/bin

MONGOS=\${BINDIR}/mongos

if ((\${#DBPORTS[@]} > 1));then

  echo -e "not support multiple route port!!!!!!!!"

  exit 1

fi

if ((\${#CONF_PORTS[@]} > 1));then

  echo -e "not support multiple config port!!!!!!!!"

  exit 1

fi


REPLNAME=${MONGO_CONF_REPL_SET_NAME}

CONFIGDB="\${REPLNAME}/"

for index in \${!CLUSTERS[@]}

do

((routeIndex=index +1));

if ((\${#CLUSTERS[@]} == \$routeIndex));then

CONFIGDB=\$CONFIGDB"\${CLUSTERS[\$index]}:\${CONF_PORTS[0]}"

else

CONFIGDB=\$CONFIGDB"\${CLUSTERS[\$index]}:\${CONF_PORTS[0]},"

fi

done

for index in \${!DBPORTS[@]}

do

((routeIndex=index +1));

DBPORT=\${DBPORTS[\$index]}

HOMEDIR=\${MONGODB_DATA_HOME}/route/db\${routeIndex}

if [ ! -d "\${HOMEDIR}" ];then

mkdir -p \${HOMEDIR}

fi

if [ ! -d "\${LOGDIR}" ];then

mkdir -p \${LOGDIR}

fi

echo -e "start mongodb route PATH: \${HOMEDIR}  port: \${DBPORT}"

nohup  numactl --interleave=all \${MONGOS}  --bind_ip "\${IP}" --port "\${DBPORT}"  --logpath "\${LOGDIR}/route.log" --pidfilepath "\${HOMEDIR}/route.pid" --logappend  --configdb="\${CONFIGDB}"  --fork  --config "\${MONGODB_CONFIG_HOME}/route.conf"	>/dev/null 2>&1 &	  

echo -e ""

done

EOF

#SHARD服务启动脚本生成
cat <<- EOF | tee ${MONGO_INSTALL_DIR}/script/startshard.sh

#!/bin/bash

DBPORTS=(\$1)

SHARD=\$2

IP=0.0.0.0

MONGODB_HOME=${MONGO_INSTALL_DIR}

MONGODB_CONFIG_HOME=${MONGO_CONF_DIR}

LOGDIR=${MONGO_LOG_DIR}

BINDIR=\${MONGODB_HOME}/bin

MONGOD=\${BINDIR}/mongod

if ((\${#DBPORTS[@]} > 1));then

  echo -e "not support multiple shard port!!!!!!!!"

  exit 1

fi

for index in \${!DBPORTS[@]}

do

DBPORT=\${DBPORTS[\$index]}

HOMEDIR=\${MONGODB_HOME}

REPLNAME=shard\${SHARD}

DATADIR=\${HOMEDIR}/data/shard/\$REPLNAME

if [ ! -d "\${HOMEDIR}" ];then

mkdir -p \${HOMEDIR}

fi

if [ ! -d "\${DATADIR}" ];then

mkdir -p \${DATADIR}

fi

if [ ! -d "\${LOGDIR}" ];then

mkdir -p \${LOGDIR}

fi

ARGS="--shardsvr --replSet \${REPLNAME} "

echo -e "start mongodb shardsrv PATH: \${DATADIR}  port: \${DBPORT}"

numactl --interleave=all \${MONGOD}  --bind_ip "\${IP}" --port "\${DBPORT}" --dbpath \${DATADIR} --logpath "\${LOGDIR}/mongod-\${REPLNAME}.log" --journal --pidfilepath "\${DATADIR}/mongod.pid" --logappend \${ARGS} --fork --config "\${MONGODB_CONFIG_HOME}/shard.conf"		  

echo -e ""

done

EOF


#环境变量配置
echo "export MONGODB_HOME=${MONGO_INSTALL_DIR}" >> /etc/profile
echo "export PATH=\$MONGODB_HOME/bin:\$PATH" >> /etc/profile

#monogodb systemd配置生成
cat <<- EOF | tee /etc/systemd/system/mongodb.service

[Unit]

Description=mongodb服务 

After=network.target

After=syslog.target


[Service]

Type=forking

ExecStart=${MONGO_INSTALL_DIR}/script/startcluster.sh start
   

[Install]

WantedBy=multi-user.target

EOF

#脚本授权
chmod 755 ${MONGO_INSTALL_DIR}/script/*.sh

#重载systemd,配置生效
systemctl daemon-reload

#mongo 集群开机启动
systemctl enable  mongodb.service

#开放防火墙端口

firewall-cmd --permanent --zone=public --add-port=${MONGO_CONF_SRV_PORT}/tcp

firewall-cmd --permanent --zone=public --add-port=${MONGO_ROUT_SRV_PORT}/tcp

firewall-cmd --permanent --zone=public --add-port=${MONGO_MMS_PORTS}/tcp

firewall-cmd --permanent --zone=public --add-port=${MONGO_MMS_BACKUP_PORTS}/tcp

for index in ${!CLUSTER_NODES[@]}

do

shardPort=$((${MONGO_SHARD_SRV_BEGIN_PORT}+$index))

firewall-cmd --permanent --zone=public --add-port=${shardPort}/tcp

done

firewall-cmd --reload


