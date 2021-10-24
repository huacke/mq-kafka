#!/bin/bash

. /etc/profile

. ~/.bash_profile

#当前机器ip
NODE_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | awk -F"/" '{print $1}')


#zk安装版本
export ZK_VERSION=3.5.5
#基础目录
export BASE_INSTALL_DIR=/usr/local
#zk安装目录
export ZK_INSTALL_DIR=/usr/local/zk
#zk数据目录
export ZK_DATA_DIR=${ZK_INSTALL_DIR}/data
#zk配置目录
export ZK_CONF_DIR=${ZK_INSTALL_DIR}/conf
#zk日志目录
export ZK_LOG_DIR=${ZK_INSTALL_DIR}/logs
#zk端口
export ZK_PORT=2181
#JMX端口
export ZK_JMXPORT=19998
#集群节点
export CLUSTER_NODES=(192.168.0.109 192.168.0.111 192.168.0.100) 

yum install axel -y

#安装zk
cd /tmp && axel -a -n 20 https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-${ZK_VERSION}/apache-zookeeper-${ZK_VERSION}-bin.tar.gz  &&  tar -xzf apache-zookeeper-${ZK_VERSION}-bin.tar.gz   -C /tmp && mv /tmp/apache-zookeeper-${ZK_VERSION}-bin ${ZK_INSTALL_DIR} &&  rm -rf /tmp/apache-zookeeper*


mkdir -p ${ZK_INSTALL_DIR}/script && mkdir -p ${ZK_DATA_DIR} && mkdir -p ${ZK_LOG_DIR}


#zk启动脚本生成
cat <<- EOF | tee ${ZK_INSTALL_DIR}/script/startZK.sh
#!/bin/bash

. /etc/profile

. ~/.bash_profile

export ZOOKEEPER_HOME=${ZK_INSTALL_DIR}

export ZOOKEEPER_BIN_HOME=\${ZOOKEEPER_HOME}/bin

export ZOOKEEPER_BIN=\${ZOOKEEPER_BIN_HOME}/zkServer.sh

export ZOOKEEPER_CONFIG_HOME=${ZK_CONF_DIR}

export ZOOKEEPER_LOG_PATH=${ZK_LOG_DIR}

export JMXPORT=${ZK_JMXPORT}

#自定义jvm参数，在zookeeper 安装的bin 目录下的zkEnv.sh 末尾加入 export SERVER_JVMFLAGS="\${ZOOKEEPER_JVM_FLAG}" 

export ZOOKEEPER_JVM_FLAG="-Xms2G -Xmx2G -Xmn1G -XX:+AlwaysPreTouch -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:-PrintGCTimeStamps -Xloggc:${ZOOKEEPER_LOG_PATH}/zookeeper_`date '+%Y%m%d%H%M%S'`.gc -XX:-UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=64M"


start()

{


hasconfigJvm=\$( cat \${ZOOKEEPER_HOME}/bin/zkEnv.sh | tail -n 5 | grep "ZOOKEEPER_JVM_FLAG=")

if [[ "$hasconfigJvm" == "" ]];then

echo -e "export ZOOKEEPER_JVM_FLAG=\\\$ZOOKEEPER_JVM_FLAG" >> \${ZOOKEEPER_BIN_HOME}/zkEnv.sh

echo -e "export SERVER_JVMFLAGS=\\\$ZOOKEEPER_JVM_FLAG" >> \${ZOOKEEPER_BIN_HOME}/zkEnv.sh

fi

echo -e "Starting  zookeeper  service    ....."

ZOO_LOG_DIR=\${ZOOKEEPER_LOG_PATH}  \${ZOOKEEPER_BIN} start-foreground  \${ZOOKEEPER_CONFIG_HOME}/zoo.cfg >>\${ZOOKEEPER_LOG_PATH}/zookeeper.log 

echo -e "Starting zookeeper service  done"

}

stop()

{

PIDS=\$(ps ax | grep -i 'zookeeper.server.quorum.QuorumPeerMain' | grep -v grep | awk '{print \$1}')

if [ "\$PIDS" == "" ]

then

echo -e "NO such process found!"

exit 1

fi

echo -e "Stopping zookeeper :{\$PIDS} ...."

kill -15 \${PIDS}

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

  Z_PIDS=\$(ps -elf | grep -i 'zookeeper.server.quorum.QuorumPeerMain' |grep -v grep |awk '{print \$4}' |xargs )

  if [ "\$C_PIDS" == "" ]

  then

 Z_STATUS="NOT running"

  else

 Z_STATUS="Running"

  fi

  echo -e "zookeeper nodes:{\$Z_PIDS}:{\${Z_STATUS}}"

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



#zk配置生成
cat <<- EOF | tee ${ZK_CONF_DIR}/zoo.cfg

tickTime=2000

initLimit=10

syncLimit=5

dataDir=${ZK_DATA_DIR}

dataLogDir=${ZK_LOG_DIR}

clientPort=${ZK_PORT}

#maxClientCnxns=60

#autopurge.purgeInterval=1

$(

for index in ${!CLUSTER_NODES[@]}

do

echo -e "server.${index}=${CLUSTER_NODES[$index]}:2888:3888"

done)

EOF



#生成myid文件
for index in ${!CLUSTER_NODES[@]}
do

if [ "$NODE_IP" == "${CLUSTER_NODES[$index]}" ];then

touch  ${ZK_DATA_DIR}/myid  && echo $index > ${ZK_DATA_DIR}/myid

fi
done

#环境变量配置
echo "export ZOOKEEPER_HOME=${ZK_INSTALL_DIR}" >> /etc/profile
echo "export PATH=\$ZOOKEEPER_HOME/bin:\$PATH" >> /etc/profile

source /etc/profile


#zk配置生成
cat <<- EOF | tee /etc/systemd/system/zookeeper.service

[Unit]

Description=zookeeper服务 

After=network.target

After=syslog.target


[Service]

Type=simple

ExecStart=${ZK_INSTALL_DIR}/script/startZK.sh start

Restart=always

SuccessExitStatus=143


[Install]

WantedBy=multi-user.target

EOF

chmod 755 ${ZK_INSTALL_DIR}/script/*.sh

#重载systemd,配置生效
systemctl daemon-reload

#zk 集群开机启动
systemctl enable  zookeeper.service

#开放防火墙端口
firewall-cmd --permanent --zone=public --add-port=${ZK_PORT}/tcp


firewall-cmd --reload