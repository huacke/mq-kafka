mq-kafka

基于最新版本spring-cloud 技术栈实现的一个kafka可靠消息队列微服务

生产端使用了消息补偿机制，即使是在消息服务停机的情况下，也能正常发送，保证消息的可靠性。

消费端使用的的模式是多线程模型，对消费线程和业务执行线程进行分离。

所使用的技术：

spring cloud

redis

zookeeper(集群)

kafka(集群)

mongodb(副本集+分片 集群)

集群搭建和配置脚本已附带



























