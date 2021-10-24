#!/bin/bash



##配置
. /etc/profile

. ~/.bash_profile



#node1
cp /opt/sentinel.conf  /var/

cp /opt/redis.conf /var/



sed -i '/^sentinel-port/c port  '"$(env | grep NODE1_SENTINEL_PORT |awk -F "=" '{print $2}')"''   /var/sentinel.conf

sed -i '/^sentinel-dir/c  dir  '"$(env | grep NODE1_SENTINEL_DIR |awk -F "=" '{print $2}')" ''/var/sentinel.conf

sed -i '/^sentinel-logfile/c logfile  '"$(env | grep NODE1_SENTINEL_LOG_FILE |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-monitor/c sentinel monitor  '"$(env | grep NODE1_SENTINEL_MONITOR |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-down-after-milliseconds/c sentinel down-after-milliseconds '"$(env | grep NODE1_SENTINEL_DOWN-AFTER-MILLISECONDS |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-failover-timeout/c sentinel failover-timeout  '"$(env | grep NODE1_SENTINEL_FAILOVER-TIMEOUT |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-auth-pass/c sentinel auth-pass '"$(env | grep NODE1_SENTINEL_AUTH-PASS |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-announce-ip/c sentinel announce-ip '"$(env | grep NODE1_IP |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-announce-port/c sentinel announce-port '"$(env | grep NODE1_SENTINEL_PORT |awk -F "=" '{print $2}')"'' /var/sentinel.conf



sed -i '/^replica-announce-ip/c replica-announce-ip '"$(env | grep NODE1_IP |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^replica-announce-port/c replica-announce-port '"$(env | grep NODE1_PORT |awk -F "=" '{print $2}')"'' /var/redis.conf

#sed -i '/^bind/c bind '"$(env | grep NODE1_IP |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^port/c port '"$(env | grep NODE1_PORT |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^pidfile/c pidfile '"$(env | grep NODE1_PID_FILE |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^logfile/c logfile '"$(env | grep NODE1_LOG_FILE |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^dir/c dir  '"$(env | grep NODE1_DIR |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^dbfilename/c dbfilename  '"$(env | grep NODE1_DB_FILENAME |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendonly/c appendonly  '"$(env | grep NODE1_APPEND_ONLY |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendfilename/c appendfilename  '"$(env | grep NODE1_APPEND_FILENAME |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendfsync/c appendfsync  '"$(env | grep NODE1_APPEND_FSYNC |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^masterauth/c masterauth  '"$(env | grep NODE1_MASTERAUTH |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^requirepass/c requirepass '"$(env | grep NODE1_REQUIREPASS |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/replicaof /c #replicaof '"$(echo  $NODE1_IP $NODE1_PORT )"'' /var/redis.conf







touch ${REIDS_HOME}/conf/${NODE1_SENTINEL_PORT}.conf

cat /var/sentinel.conf > ${REIDS_HOME}/conf/${NODE1_SENTINEL_PORT}.conf

touch   ${REIDS_HOME}/conf/${NODE1_PORT}.conf

cat  /var/redis.conf > ${REIDS_HOME}/conf/${NODE1_PORT}.conf



rm -rf  /var/sentinel.conf 

rm -rf /var/redis.conf







#node2
cp /opt/sentinel.conf  /var/

cp /opt/redis.conf /var/



sed -i '/^sentinel-port/c port  '"$(env | grep NODE2_SENTINEL_PORT |awk -F "=" '{print $2}')"''   /var/sentinel.conf

sed -i '/^sentinel-dir/c  dir  '"$(env | grep NODE2_SENTINEL_DIR |awk -F "=" '{print $2}')" ''/var/sentinel.conf

sed -i '/^sentinel-logfile/c logfile  '"$(env | grep NODE2_SENTINEL_LOG_FILE |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-monitor/c sentinel monitor  '"$(env | grep NODE2_SENTINEL_MONITOR |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-down-after-milliseconds/c sentinel down-after-milliseconds '"$(env | grep NODE2_SENTINEL_DOWN-AFTER-MILLISECONDS |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-failover-timeout/c sentinel failover-timeout  '"$(env | grep NODE2_SENTINEL_FAILOVER-TIMEOUT |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-auth-pass/c sentinel auth-pass '"$(env | grep NODE2_SENTINEL_AUTH-PASS |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-announce-ip/c sentinel announce-ip '"$(env | grep NODE2_IP |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-announce-port/c sentinel announce-port '"$(env | grep NODE1_SENTINEL_PORT |awk -F "=" '{print $2}')"'' /var/sentinel.conf



sed -i '/^replica-announce-ip/c replica-announce-ip '"$(env | grep NODE2_IP |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^replica-announce-port/c replica-announce-port '"$(env | grep NODE2_PORT |awk -F "=" '{print $2}')"'' /var/redis.conf

#sed -i '/^bind/c bind '"$(env | grep NODE2_IP |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^port/c port '"$(env | grep NODE2_PORT |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^pidfile/c pidfile '"$(env | grep NODE2_PID_FILE |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^logfile/c logfile '"$(env | grep NODE2_LOG_FILE |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^dir/c dir  '"$(env | grep NODE2_DIR |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^dbfilename/c dbfilename  '"$(env | grep NODE2_DB_FILENAME |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendonly/c appendonly  '"$(env | grep NODE2_APPEND_ONLY |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendfilename/c appendfilename  '"$(env | grep NODE2_APPEND_FILENAME |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendfsync/c appendfsync  '"$(env | grep NODE2_APPEND_FSYNC |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^masterauth/c masterauth  '"$(env | grep NODE2_MASTERAUTH |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^requirepass/c requirepass '"$(env | grep NODE2_REQUIREPASS |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/replicaof /c replicaof '"$(echo  $NODE1_IP $NODE1_PORT )"'' /var/redis.conf



touch ${REIDS_HOME}/conf/${NODE2_SENTINEL_PORT}.conf

cat /var/sentinel.conf > ${REIDS_HOME}/conf/${NODE2_SENTINEL_PORT}.conf

touch   ${REIDS_HOME}/conf/${NODE2_PORT}.conf

cat  /var/redis.conf > ${REIDS_HOME}/conf/${NODE2_PORT}.conf



rm -rf  /var/sentinel.conf 

rm -rf /var/redis.conf



#node3

cp /opt/sentinel.conf  /var/

cp /opt/redis.conf /var/



#sed -i '/^bind/c bind '"$(env | grep NODE3_IP |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^sentinel-port/c port  '"$(env | grep NODE3_SENTINEL_PORT |awk -F "=" '{print $2}')"''   /var/sentinel.conf

sed -i '/^sentinel-dir/c  dir  '"$(env | grep NODE3_SENTINEL_DIR |awk -F "=" '{print $2}')" ''/var/sentinel.conf

sed -i '/^sentinel-logfile/c logfile  '"$(env | grep NODE3_SENTINEL_LOG_FILE |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-monitor/c sentinel monitor  '"$(env | grep NODE3_SENTINEL_MONITOR |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-down-after-milliseconds/c sentinel down-after-milliseconds '"$(env | grep NODE3_SENTINEL_DOWN-AFTER-MILLISECONDS |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-failover-timeout/c sentinel failover-timeout  '"$(env | grep NODE3_SENTINEL_FAILOVER-TIMEOUT |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-auth-pass/c sentinel auth-pass '"$(env | grep NODE3_SENTINEL_AUTH-PASS |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-announce-ip/c sentinel announce-ip '"$(env | grep NODE3_IP |awk -F "=" '{print $2}')"'' /var/sentinel.conf

sed -i '/^sentinel-announce-port/c sentinel announce-port '"$(env | grep NODE1_SENTINEL_PORT |awk -F "=" '{print $2}')"'' /var/sentinel.conf



sed -i '/^replica-announce-ip/c replica-announce-ip '"$(env | grep NODE3_IP |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^replica-announce-port/c replica-announce-port '"$(env | grep NODE3_PORT |awk -F "=" '{print $2}')"'' /var/redis.conf



sed -i '/^port/c port '"$(env | grep NODE3_PORT |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^pidfile/c pidfile '"$(env | grep NODE3_PID_FILE |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^logfile/c logfile '"$(env | grep NODE3_LOG_FILE |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^dir/c dir  '"$(env | grep NODE3_DIR |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^dbfilename/c dbfilename  '"$(env | grep NODE3_DB_FILENAME |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendonly/c appendonly  '"$(env | grep NODE3_APPEND_ONLY |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendfilename/c appendfilename  '"$(env | grep NODE3_APPEND_FILENAME |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^appendfsync/c appendfsync  '"$(env | grep NODE3_APPEND_FSYNC |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^masterauth/c masterauth  '"$(env | grep NODE3_MASTERAUTH |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/^requirepass/c requirepass '"$(env | grep NODE3_REQUIREPASS |awk -F "=" '{print $2}')"'' /var/redis.conf

sed -i '/replicaof /c replicaof '"$(echo $NODE1_IP $NODE1_PORT )"'' /var/redis.conf



touch ${REIDS_HOME}/conf/${NODE3_SENTINEL_PORT}.conf

cat /var/sentinel.conf > ${REIDS_HOME}/conf/${NODE3_SENTINEL_PORT}.conf

touch   ${REIDS_HOME}/conf/${NODE3_PORT}.conf

cat  /var/redis.conf > ${REIDS_HOME}/conf/${NODE3_PORT}.conf



rm -rf  /var/sentinel.conf 

rm -rf /var/redis.conf





