//常用命令

docker exec  -it kafka-cluster-dev    /opt/kafka/bin/kafka-topics.sh --bootstrap-server 192.168.0.109:19093,192.168.0.109:19094,192.168.0.109:19095 --topic TEST --replication-factor 3 --partitions 3 --create --command-config  /opt/kafka/config/topic.properties
docker exec  -it kafka-cluster-dev    /opt/kafka/bin/kafka-topics.sh --bootstrap-server 192.168.0.109:19093,192.168.0.109:19094,192.168.0.109:19095 --topic PAY --replication-factor 3 --partitions 3 --create --command-config   /opt/kafka/config/topic.properties

docker exec  -it kafka-cluster-dev kafka-console-consumer.sh --bootstrap-server 192.168.0.109:19093,192.168.0.109:19094,192.168.0.109:19095 --topic TEST --from-beginning --consumer.config  /opt/kafka/config/consumer.properties 

docker exec  -it kafka-cluster-dev kafka-console-producer.sh   --broker-list 192.168.0.109:19093,192.168.0.109:19094,192.168.0.109:19095 --topic TEST   --producer.config  /opt/kafka/config/producer.properties