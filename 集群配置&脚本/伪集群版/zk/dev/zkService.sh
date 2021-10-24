#! /bin/bash

# chkconfig:   2345 90 10

# description:  Redis is a persistent key-value database

#systemctl start sshd  

#sh /opt/zookeeper/bin/zkServer.sh start-foreground  /opt/zookeeper/conf/zoo1.cfg &

#sh /opt/zookeeper/bin/zkServer.sh  start-foreground  /opt/zookeeper/conf/zoo2.cfg & 

#sh /opt/zookeeper/bin/zkServer.sh  start-foreground  /opt/zookeeper/conf/zoo3.cfg





exec nohup sh /opt/zookeeper/bin/zkServer.sh start-foreground /opt/zookeeper/conf/zoo1.cfg >>/data/zk/log.log & 

exec nohup sh /opt/zookeeper/bin/zkServer.sh start-foreground /opt/zookeeper/conf/zoo2.cfg>> /data/zk/log.log &

exec nohup sh /opt/zookeeper/bin/zkServer.sh start-foreground /opt/zookeeper/conf/zoo3.cfg>> /data/zk/log.log & 



tail -fn 3000 /data/zk/log.log 

