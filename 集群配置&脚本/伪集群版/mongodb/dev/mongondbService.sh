#! /bin/bash
# chkconfig:   2345 90 10
# description: exec nohup mongodb is a persistent key-value database
#clear cache
echo 3 > /proc/sys/vm/drop_caches
#db
exec nohup mongod -f /usr/local/mongodb/conf/node1/shard1.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node1/shard2.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node1/shard3.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node2/shard1.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node2/shard2.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node2/shard3.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node3/shard1.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node3/shard2.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node3/shard3.conf >>/data/mongodb/log.log& 

#config
exec nohup mongod -f /usr/local/mongodb/conf/node1/configServer.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node2/configServer.conf >>/data/mongodb/log.log& 
exec nohup mongod -f /usr/local/mongodb/conf/node3/configServer.conf >>/data/mongodb/log.log& 

#route
exec nohup mongos -f /usr/local/mongodb/conf/node1/route.conf >>/data/mongodb/log.log& 
exec nohup mongos -f /usr/local/mongodb/conf/node2/route.conf >>/data/mongodb/log.log& 
exec nohup mongos -f /usr/local/mongodb/conf/node3/route.conf >>/data/mongodb/log.log& 

tail -fn 3000 /data/mongodb/log.log
