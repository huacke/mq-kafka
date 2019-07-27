#! /bin/bash
# chkconfig:   2345 90 10
# description:  Redis is a persistent key-value database


LOG_DIR=/data/kafka/node1/logs JMX_PORT=17777 exec nohup kafka-server-start.sh /opt/kafka/config/server1.properties >>/data/kafka/log.log &
LOG_DIR=/data/kafka/node2/logs JMX_PORT=18888 exec nohup kafka-server-start.sh /opt/kafka/config/server2.properties >>/data/kafka/log.log &
LOG_DIR=/data/kafka/node3/logs JMX_PORT=19999 exec nohup kafka-server-start.sh /opt/kafka/config/server3.properties >>/data/kafka/log.log &
tail -fn 3000 /data/kafka/log.log

