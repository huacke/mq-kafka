#! /bin/bash


#当前机器ip
NODE_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | awk -F"/" '{print $1}')

export SCALA_VERSION=2.12

#scala版本
export SCALA_FULL_VERSION=${SCALA_VERSION}.8

#kafka安装版本
export KAFKA_VERSION=2.3.0

#基础目录
export BASE_INSTALL_DIR=/usr/local

#scala安装目录
export SCALA_INSTALL_DIR=${BASE_INSTALL_DIR}/scala

#kafka安装目录
export KAFKA_INSTALL_DIR=${BASE_INSTALL_DIR}/kafka

#kafka数据目录
export KAFKA_DATA_DIR=${KAFKA_INSTALL_DIR}/data

#kafka配置目录
export KAFKA_CONF_DIR=${KAFKA_INSTALL_DIR}/config

#kafka日志目录
export KAFKA_LOG_DIR=${KAFKA_INSTALL_DIR}/logs

#kafka端口
export KAFKA_PORT=9092

#jmx端口
export KAFKA_JMX_PORT=19999

#zk端口
export ZK_PORT=2181

#kafka在zk中注册节点名称
KAFKA_ZK_SPACE_NAME=kafka

#kafka外网ip
export NETWORK_IP=$NODE_IP

#kafka外网端口
export NETWORK_PORT=$KAFKA_PORT

#kafka SASL 认证用户名
export KAFKA_USER_NAME=kafka

#kafka SASL 认证密码
export KAFKA_USER_PASSWD=kafka

#zk集群节点
export ZK_CLUSTER_NODES=(192.168.0.109 192.168.0.111 192.168.0.100)

#kafka集群节点
export CLUSTER_NODES=(192.168.0.109 192.168.0.111 192.168.0.100) 

#kafka服务端jvm参数
export KAFKA_SERVER_OPTS="'-Xmx2G -Xms2G -Djava.security.auth.login.config=${KAFKA_CONF_DIR}/kafka_server_jaas.conf'"

#kafka客户端jvm参数
export KAFKA_CLIENT_OPTS="-Djava.security.auth.login.config=${KAFKA_CONF_DIR}/kafka_client_jaas.conf"

yum install axel -y

#安装scala
cd /tmp && rm -rf /tmp/scala* && axel -a -n 30 https://downloads.lightbend.com/scala/${SCALA_FULL_VERSION}/scala-${SCALA_FULL_VERSION}.tgz  &&  tar -xzf scala-${SCALA_FULL_VERSION}.tgz   -C /tmp && mv /tmp/scala-${SCALA_FULL_VERSION} ${SCALA_INSTALL_DIR} &&  rm -rf /tmp/scala*

#安装kafka
cd /tmp &&  rm -rf /tmp/kafka_*  && axel -a -n 30 http://mirrors.tuna.tsinghua.edu.cn/apache/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz  &&  tar -xzf kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz   -C /tmp && mv /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION} ${KAFKA_INSTALL_DIR} &&  rm -rf /tmp/kafka_*

mkdir -p ${KAFKA_INSTALL_DIR}/script && mkdir -p ${KAFKA_DATA_DIR} && mkdir -p ${KAFKA_LOG_DIR}

#配置环境变量
cat <<- EOF | tee -a  /etc/profile

export SCALA_HOME=${SCALA_INSTALL_DIR}
export PATH=\$SCALA_HOME/bin:\$PATH

export KAFKA_HOME=${KAFKA_INSTALL_DIR}
export PATH=\$KAFKA_HOME/bin:\$PATH

EOF

#配置环境变量生效
source /etc/profile


#生成brokerId
for index in ${!CLUSTER_NODES[@]}
do
if [ "$NODE_IP" == "${CLUSTER_NODES[$index]}" ];then
export brokerId=$(($index+1))
fi
done

#配置ZK集群地址
export ZK_CONNECT_ADDR=""

for index in ${!ZK_CLUSTER_NODES[@]}
do
if [ "$index" == "$((${#ZK_CLUSTER_NODES[@]}-1))" ];then
ZK_CONNECT_ADDR="$ZK_CONNECT_ADDR${ZK_CLUSTER_NODES[$index]}:${ZK_PORT}"
else
ZK_CONNECT_ADDR="$ZK_CONNECT_ADDR${ZK_CLUSTER_NODES[$index]}:${ZK_PORT},"
fi
done

export ZK_CONNECT_ADDR =$ZK_CONNECT_ADDR/$KAFKA_ZK_SPACE_NAME

#配置环境变量
cat <<- EOF | tee   ${KAFKA_CONF_DIR}/server.properties

#brokerId  集群每个节点ID要唯一
broker.id=${brokerId}

listeners=SASL_PLAINTEXT://0.0.0.0:${KAFKA_PORT}

#暴露外网地址
advertised.listeners=SASL_PLAINTEXT://${NETWORK_IP}:${NETWORK_PORT}

#kafka SASL认证
security.inter.broker.protocol=SASL_PLAINTEXT

sasl.enabled.mechanisms=PLAIN

sasl.mechanism.inter.broker.protocol=PLAIN

authorizer.class.name = kafka.security.auth.SimpleAclAuthorizer

allow.everyone.if.no.acl.found=true

super.users=User:kafka


#处理网络请求的线程数量
num.network.threads=8

#用来处理磁盘IO的线程数量
num.io.threads=8

#发送套接字的缓冲区大小
socket.send.buffer.bytes=102400

#接受套接字的缓冲区大小
socket.receive.buffer.bytes=102400

#请求套接字的缓冲区大小
socket.request.max.bytes=104857600

#kafka运行日志存放的路径
log.dirs=${KAFKA_DATA_DIR}

#topic在当前broker上的分片个数
num.partitions=3

#用来恢复和清理data下数据的线程数量
num.recovery.threads.per.data.dir=1

#segment文件保留的最长时间，超时将被删除
log.retention.hours=240

#滚动生成新的segment文件的最大时间
log.roll.hours=240

#日志文件中每个segment的大小，默认为1G
log.segment.bytes=1073741824

#周期性检查文件大小的时间
log.retention.check.interval.ms=300000

#日志清理是否打开
log.cleaner.enable=true

#broker需要使用zookeeper保存meta数据
zookeeper.connect=${ZK_CONNECT_ADDR}

#zookeeper链接超时时间
zookeeper.connection.timeout.ms=6000

#partion buffer中，消息的条数达到阈值，将触发flush到磁盘
log.flush.interval.messages=5000

#消息buffer的时间，达到阈值，将触发flush到磁盘
log.flush.interval.ms=1000

#删除topic需要server.properties中设置delete.topic.enable=true否则只是标记删除
delete.topic.enable=false

#当producer设置request.required.acks为-1时，min.insync.replicas指定replicas的最小数目（必须确认每一个repica的写数据都是成功的），如果这个数目没有达到，producer会产生异常。
min.insync.replicas=2

#指明了是否能够使不在ISR中replicas设置用来作为leader
unclean.leader.election.enable=false

#是否自动创建主题
auto.create.topics.enable=false

#默认创建的副本数量
default.replication.factor=3

EOF


#配置环境变量

cat <<- EOF | tee  ${KAFKA_INSTALL_DIR}/script/startkafka.sh
#! /bin/bash

#当前机器ip
NODE_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | awk -F"/" '{print $1}')

#kafka安装目录
KAFKA_HOME=${KAFKA_INSTALL_DIR}

#kafka bin目录
KAFKA_BIN_HOME=\${KAFKA_HOME}/bin

#kafka启动脚本
KAFKA_BIN=\${KAFKA_BIN_HOME}/kafka-server-start.sh

#kafka配置目录
KAFKA_CONFIG_HOME=${KAFKA_CONF_DIR}

#jmx端口
export JMX_PORT=${KAFKA_JMX_PORT}

#kafka日志目录
KAFKA_LOG_PATH=${KAFKA_LOG_DIR}

#导出日志路径
export LOG_DIR=\${KAFKA_LOG_PATH}

#jmx参数配置
export KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote=true -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=\${NODE_IP}"
#kafka jvm 参数
export KAFKA_SERVER_OPTS=${KAFKA_SERVER_OPTS}

source /etc/profile


start()

{

echo "never" > /sys/kernel/mm/transparent_hugepage/enabled

echo "never" > /sys/kernel/mm/transparent_hugepage/defrag

sysctl -w vm.max_map_count=262144

sysctl -w vm.swappiness=1

sysctl -w vm.zone_reclaim_mode=0

echo 0 > /proc/sys/vm/zone_reclaim_mode

sysctl -w vm.overcommit_memory=1

sysctl -w net.core.somaxconn=2056

echo -e "Starting  kafka  service    ....."

\${KAFKA_BIN} -daemon  \${KAFKA_CONFIG_HOME}/server.properties

#休眠一会，等待kafka jmx进程 启动
sleep 10

${KAFKA_INSTALL_DIR}/script/startkafkafirewall.sh start

echo -e "Starting kafka service  done"

}

stop()
{

PIDS=\$(ps ax | grep -i 'kafka.Kafka' | grep -v grep | awk '{print \$1}')

if [ "\$PIDS" == "" ];then

   echo -e "NO such process found!"

   firewall-cmd --reload

   exit 1

fi

echo -e "Stopping kafka  service  :{\$PIDS} ...."

kill -15 \${PIDS}

exit 

}

restart()

{

    echo "INFO : kafka  service is stoping ... "

    stop

    echo "INFO : kafka  service is starting ..."

    start

}


status()
{

  Z_PIDS=\$(ps -elf | grep -i 'kafka.Kafka' |grep -v grep |awk '{print \$4}' |xargs )

  if [ "\$C_PIDS" == "" ];then

 Z_STATUS="NOT running"

  else

  
 Z_STATUS="Running"

  fi

  echo -e "kafka nodes:{\$Z_PIDS}:{\${Z_STATUS}}"

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


#生成jmx防火墙脚本

cat <<- EOF | tee  ${KAFKA_INSTALL_DIR}/script/startkafkafirewall.sh 
#! /bin/bash

##################################################################

#   FileName startkafkafirewall.sh

#   Description start kafkafirewall  service

#   使用JMX监控kafka，kafka jmx 启动时会有多个端口，因为它每次启动时除了固定的一个jmx端口，还会会随机分配几个端口

#   需要把这几个随机端口也开放

#################################################################

start()

{

echo -e "Starting kafka firewall ......."

addFirewallPort

echo -e "Started kafka firewall"

}

addFirewallPort()

{

R_PIDS=\$(ps -elf | grep  'kafka.Kafka' |grep -v grep |awk '{print \$4}'|xargs)

if test "\$R_PIDS" != "" ;then

	for index in \${!R_PIDS[@]}

	do

		currentPort=\${R_PIDS[\$index]}

		N_PORTS=\$(netstat -antlp | grep \$currentPort | awk  '{print \$4}'|awk -F[:]  '{print \$2}'|uniq -u|xargs)

		if test "\$N_PORTS" != "" ;then

			N_PORTS=(\$N_PORTS)

			for pindex in \${!N_PORTS[@]}

			do

				firewall-cmd --zone=public --add-port=\${N_PORTS[\$pindex]}/tcp

			done

		fi

	done

fi

firewall-cmd --reload

}

stop()
{

exit 0

}

restart()

{



start

exit 0 

}

status()

{

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


#kafka kafka_server_jaas.conf 配置生成

cat <<- EOF | tee ${KAFKA_CONF_DIR}/kafka_server_jaas.conf

KafkaServer {

    org.apache.kafka.common.security.plain.PlainLoginModule required

    username="${KAFKA_USER_NAME}"

    password="${KAFKA_USER_PASSWD}"

    user_${KAFKA_USER_NAME}="${KAFKA_USER_PASSWD}";

};

EOF



#kafka kafka_client_jaas.conf 配置生成

cat <<- EOF | tee ${KAFKA_CONF_DIR}/kafka_client_jaas.conf

KafkaClient {

        org.apache.kafka.common.security.plain.PlainLoginModule required

        username="${KAFKA_USER_NAME}"

        password="${KAFKA_USER_PASSWD}";

};

EOF


touch    ${KAFKA_CONF_DIR}/topic.properties

#配置 SASL安全认证
sed '5 a export KAFKA_OPTS="${KAFKA_SERVER_OPTS}"'  -i  ${KAFKA_INSTALL_DIR}/bin/kafka-server-start.sh
sed '5 a export KAFKA_OPTS="'${KAFKA_CLIENT_OPTS}'"'  -i  ${KAFKA_INSTALL_DIR}/bin/kafka-console-producer.sh
sed '5 a export KAFKA_OPTS="'${KAFKA_CLIENT_OPTS}'"'  -i  ${KAFKA_INSTALL_DIR}/bin/kafka-console-consumer.sh
sed '5 a export KAFKA_OPTS="'${KAFKA_CLIENT_OPTS}'"'  -i  ${KAFKA_INSTALL_DIR}/bin/kafka-topics.sh



sed '6 a security.protocol=SASL_PLAINTEXT'  -i  ${KAFKA_CONF_DIR}/producer.properties
sed '7 a sasl.mechanism=PLAIN'  -i  ${KAFKA_CONF_DIR}/producer.properties

sed '6 a security.protocol=SASL_PLAINTEXT'  -i  ${KAFKA_CONF_DIR}/consumer.properties
sed '7 a sasl.mechanism=PLAIN'  -i  ${KAFKA_CONF_DIR}/consumer.properties

echo "security.protocol=SASL_PLAINTEXT" >>  ${KAFKA_CONF_DIR}/topic.properties
sed '1 a sasl.mechanism=PLAIN'  -i  ${KAFKA_CONF_DIR}/topic.properties


#kafka systemd 配置生成
cat <<- EOF | tee /etc/systemd/system/kafka.service

[Unit]

Description=kafka服务 

After=network.target

After=syslog.target


[Service]

Type=forking

ExecStart=${KAFKA_INSTALL_DIR}/script/startkafka.sh start

ExecStopPost=${KAFKA_INSTALL_DIR}/script/startkafka.sh stop

SuccessExitStatus= 0 143 137 SIGTERM SIGKILL

LimitCORE=infinity
LimitNOFILE=100000
LimitNPROC=100000
   

[Install]

WantedBy=multi-user.target

EOF


#调整limit参数
echo -e " * hard nofile 65536 \n * soft nofile 65536 \n * hard memlock unlimited \n * hard nproc 4096 \n * hard as unlimited \n"  >>/etc/security/limits.conf

ulimit -Hn


chmod 755 ${KAFKA_INSTALL_DIR}/script/*.sh


#重载systemd,配置生效
systemctl daemon-reload

#kafka 集群开机启动
systemctl enable  kafka.service

#开放防火墙端口
firewall-cmd --permanent --zone=public --add-port=${KAFKA_PORT}/tcp

firewall-cmd --permanent --zone=public --add-port=${KAFKA_JMX_PORT}/tcp

firewall-cmd --reload











































