#!/bin/bash


. /etc/profile
. ~/.bash_profile

#当前机器ip
NODE_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | awk -F"/" '{print $1}')
#TCL安装版本
TCL_VERSION=8.6.9
#redis安装版本
REDIS_VERSION=5.0.5
#基础目录
BASE_INSTALL_DIR=/usr/local
#redis安装目录
REDIS_INSTALL_DIR=/usr/local/redis
#redis端口
REDIS_PORT=6379
#redis认证密码
REDIS_PASSWD=root
#哨兵端口
SENTINEL_PORT=26379
#主redis ip
MASTER_IP=192.168.0.109
#主redis 端口
MASTER_PORT=6379
#哨兵集群名称
CLUSTER_NAME=master
#redis集群ip
CLUSTER_NODES=(192.168.0.109 192.168.0.111 192.168.0.100) 

yum install axel

#安装tcl
cd /tmp && axel -a -n 20 http://downloads.sourceforge.net/tcl/tcl${TCL_VERSION}-src.tar.gz &&  tar -xzf tcl${TCL_VERSION}-src.tar.gz   -C /tmp \
    && mv /tmp/tcl${TCL_VERSION} ${BASE_INSTALL_DIR} \
    && cd  ${BASE_INSTALL_DIR}/tcl${TCL_VERSION}/unix/ && ./configure && make && make install &&  rm -rf /tmp/tcl*

#安装redis
cd /tmp && mkdir -p ${REDIS_INSTALL_DIR} && axel -a -n 20  http://download.redis.io/releases/redis-${REDIS_VERSION}.tar.gz && tar -xzf redis-${REDIS_VERSION}.tar.gz  -C /tmp  \
        && mv /tmp/redis-${REDIS_VERSION} ${REDIS_INSTALL_DIR} && cd  ${REDIS_INSTALL_DIR}/redis-${REDIS_VERSION} && make && make test && make install && rm -rf  /tmp/redis-*

mkdir -p ${REDIS_INSTALL_DIR}/{conf,script,data,logs}


#redis配置生成
cat <<- EOF | tee ${REDIS_INSTALL_DIR}/conf/${REDIS_PORT}.conf
protected-mode no

bind 0.0.0.0

port ${REDIS_PORT}

tcp-backlog 511

timeout 0

tcp-keepalive 300

daemonize yes

supervised systemd

pidfile "/var/run/redis_${REDIS_PORT}.pid"

loglevel notice

logfile "${REDIS_INSTALL_DIR}/logs/${REDIS_PORT}.log"

databases 16

#save ""

save 900 1
save 300 10
save 60 10000

stop-writes-on-bgsave-error yes

rdbchecksum yes

dbfilename "${REDIS_PORT}.rdb"

dir "${REDIS_INSTALL_DIR}/data/${REDIS_PORT}"

replica-serve-stale-data yes

replica-read-only yes

repl-diskless-sync-delay 5

repl-disable-tcp-nodelay no

replica-priority 100

masterauth "${REDIS_PASSWD}"

requirepass "${REDIS_PASSWD}"

#rename-command CONFIG ""

#maxclients 10000

#maxmemory-policy volatile-lru

appendonly yes

appendfilename "${REDIS_PORT}.aof"

appendfsync everysec

no-appendfsync-on-rewrite no

auto-aof-rewrite-percentage 100

auto-aof-rewrite-min-size 64mb

aof-rewrite-incremental-fsync yes

aof-load-truncated yes

lua-time-limit 5000

slowlog-log-slower-than 10000

slowlog-max-len 128

latency-monitor-threshold 0

notify-keyspace-events ""

hash-max-ziplist-entries 512

hash-max-ziplist-value 64

list-max-ziplist-size -2

list-compress-depth 0

set-max-intset-entries 512

zset-max-ziplist-entries 128

zset-max-ziplist-value 64

hll-sparse-max-bytes 3000

activerehashing yes

client-output-buffer-limit normal 0 0 0
client-output-buffer-limit replica 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60

hz 10

$(echo $(if [ "$NODE_IP" != "$MASTER_IP" ];then
echo replicaof ${MASTER_IP} ${MASTER_PORT}
fi))

EOF



#哨兵配置生成
cat <<- EOF | tee ${REDIS_INSTALL_DIR}/conf/${SENTINEL_PORT}.conf

daemonize yes
supervised systemd
protected-mode no
port ${SENTINEL_PORT}
dir "${REDIS_INSTALL_DIR}/data/${SENTINEL_PORT}"
logfile "${REDIS_INSTALL_DIR}/logs/${SENTINEL_PORT}.log"
sentinel monitor ${CLUSTER_NAME} ${MASTER_IP} ${MASTER_PORT} 2
sentinel down-after-milliseconds ${CLUSTER_NAME} 5000
sentinel failover-timeout ${CLUSTER_NAME} 30000
sentinel auth-pass ${CLUSTER_NAME} ${REDIS_PASSWD}

EOF


#集群启动脚本生成 startcluster.sh 
cat <<- EOF | tee ${REDIS_INSTALL_DIR}/script/startcluster.sh 
#!/bin/bash

. /etc/profile
. ~/.bash_profile

export  SHELL_REDIS=${REDIS_INSTALL_DIR}/script/startRedis.sh
export  SHELL_SENTINEL=${REDIS_INSTALL_DIR}/script/startSentinel.sh

sysctl -w vm.overcommit_memory=1
sysctl -w net.core.somaxconn=2056
echo "never" > /sys/kernel/mm/transparent_hugepage/enabled
echo "never" > /sys/kernel/mm/transparent_hugepage/defrag

start()
{

sh \$SHELL_REDIS start
sh \$SHELL_SENTINEL start

}

stop()

{

sh \$SHELL_REDIS stop
sh \$SHELL_SENTINEL stop

}

restart()
{
    stop
    start
}


usage()

{

 echo -e "Usage: \$0 [start|restart|stop]"

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

*)

usage

;;

esac

EOF


#启动redis脚本生成 startRedis.sh 
cat <<- EOF | tee ${REDIS_INSTALL_DIR}/script/startRedis.sh 
#!/bin/bash

. /etc/profile
. ~/.bash_profile

export RPORTS=(${REDIS_PORT})
export BASE_HONME=${REDIS_INSTALL_DIR}
export EXEC=/usr/local/bin/redis-server
export CLIEXEC=/usr/local/bin/redis-cli
export PWD="${REDIS_PASSWD}"

start()
{

echo -e "Starting  redis-server nodes {\${RPORTS[@]}} ....."

CPORTS=(\$(ps ax | grep -i 'redis-server' |grep -v 'sentinel' | grep -v grep | awk '{print \$6}' | awk -F ':'  '{print \$2}' | xargs))


for index in \${!RPORTS[@]}

do


if echo "\${CPORTS[@]}" | grep -w "\${RPORTS[\$index]}" &>/dev/null; then
    echo " redis-server port \${RPORTS[\$index]}  process is already running ingroe ..."
    continue
else


if [ ! -d  \${BASE_HONME}/data/\${RPORTS[\$index]} ]; then

 mkdir -p  \${BASE_HONME}/data/\${RPORTS[\$index]}

fi

CONF="\${BASE_HONME}/conf/\${RPORTS[\$index]}.conf"

echo -e "Starting redis-server port: \${RPORTS[\$index]} ............ "

\$EXEC \$CONF

fi

done

}

stop()

{

echo -e "Stopping redis-server all :\${RPORTS[@]} ...."

for index in \${!RPORTS[@]}
do
   
   PID=\$(ps ax | grep -i 'redis-server' |grep -v 'sentinel' | grep -i \${RPORTS[\$index]} | grep -v grep | awk '{print \$1}' | xargs)
   if [ "\$PID" == "" ];then
   echo -e "NO such port \${RPORTS[\$index]} process found! ingore ...." 
   else
   echo -e "Stopping redis-server :\${RPORTS[\$index]} ...."   
   \$CLIEXEC -a \$PWD  -p \${RPORTS[\$index]} shutdown
    while [ -x /proc/\${PID[0]} ]
       do
          echo -e "Waiting for redis server prot: \${RPORTS[\$index]}  to shutdown ..."
          sleep 1
       done
    echo -e "Stopping redis-server :\${RPORTS[\$index]} done"
   fi   
done


}

restart()
{
    stop
    start
    exit 1
}


status()

{

  R_PIDS=\$(ps ax | grep -i 'redis-server' |grep -v 'sentinel' | grep -v grep | awk '{print \$1}' | xargs )

  echo -e "redis-server all nodes:{\${RPORTS[@]}}: running nodes pid is {\${R_PIDS}}"

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


#启动哨兵脚本生成 startSentinel.sh 
cat <<- EOF | tee ${REDIS_INSTALL_DIR}/script/startSentinel.sh

#!/bin/bash

. /etc/profile
. ~/.bash_profile

export RPORTS=(${SENTINEL_PORT})
export BASE_HONME=${REDIS_INSTALL_DIR}
export EXEC=/usr/local/bin/redis-server
export CLIEXEC=/usr/local/bin/redis-cli
export PWD="${REDIS_PASSWD}"

start()
{

echo -e "Starting  redis-sentinel nodes {\${RPORTS[@]}} ....."

CPORTS=(\$(ps ax | grep -i 'redis-server' |grep -i 'sentinel' | grep -v grep | awk '{print $6}' | awk -F ':'  '{print \$2}' | xargs))

for index in \${!RPORTS[@]}

do

if echo "\${CPORTS[@]}" | grep -w "\${RPORTS[\$index]}" &>/dev/null; then
    echo " redis-server port \${RPORTS[\$index]}  process is already running"
    continue
else

if [ ! -d  \${BASE_HONME}/data/\${RPORTS[\$index]} ]; then
   mkdir -p  \${BASE_HONME}/data/\${RPORTS[\$index]}
fi

CONF="\${BASE_HONME}/conf/\${RPORTS[\$index]}.conf"

echo -e "Starting redis-sentinel port: \${RPORTS[\$index]} ............ "

\$EXEC \$CONF --sentinel

fi

done


}

stop()

{

echo -e "Stopping redis-sentinel all :\${RPORTS[@]} ...."

for index in \${!RPORTS[@]}
do
   
   PID=\$(ps ax | grep -i 'redis-server' |grep -i 'sentinel' | grep -i \${RPORTS[\$index]} | grep -v grep | awk '{print \$1}' | xargs)
   if [ "\$PID" == "" ];then
   echo -e "NO such port \${RPORTS[\$index]} process found! ingore ...." 
   else
   echo -e "Stopping redis-server :\${RPORTS[\$index]} ...."   
   \$CLIEXEC -a \$PWD  -p \${RPORTS[\$index]} shutdown
    while [ -x /proc/\${PID[0]} ]
       do
          echo -e "Waiting for redis-sentinel prot: \${RPORTS[\$index]}  to shutdown ..."
          sleep 1
       done
    echo -e "Stopping redis-sentinel :\${RPORTS[\$index]} done"
   fi   
done


}

restart()
{
    stop
    start
    exit 1
}


status()

{

  R_PIDS=\$(ps ax | grep -i 'redis-server' |grep -i 'sentinel' | grep -v grep | awk '{print \$1}' | xargs )

  echo -e "redis-sentinel all nodes:{\${RPORTS[@]}}: running nodes pid is {\${R_PIDS}}"

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

chmod +x ${REDIS_INSTALL_DIR}/script/*.sh



#systemd 服务启动配置生成
cat <<- EOF | tee /etc/systemd/system/redis.service

[Unit]
Description=Redis In-Memory Data Store
After=network.target
 
[Service]
Type=forking
ExecStart=${REDIS_INSTALL_DIR}/script/startcluster.sh start
ExecStop=${REDIS_INSTALL_DIR}/script/startcluster.sh stop
Restart=always
 
[Install]
WantedBy=multi-user.target

EOF

#重载systemd,配置生效
systemctl daemon-reload
#redis 集群开机启动
systemctl enable  redis.service

#开放防火墙端口
firewall-cmd --permanent --zone=public --add-port=${REDIS_PORT}/tcp
firewall-cmd --permanent --zone=public --add-port=${SENTINEL_PORT}/tcp
firewall-cmd --reload


	
