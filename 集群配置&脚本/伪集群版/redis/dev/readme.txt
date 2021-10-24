//常用命令：
进入容器

docker exec  -it redis-sentinel-cluster-dev /bin/bash

注： 
1，sentinel.conf redis.conf 为模板配置文件
2,  redis 和 sentinel 相关参数配置在 node*.env 文件中， 构造镜像前需要先配置好




