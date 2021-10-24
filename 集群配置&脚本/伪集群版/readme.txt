目前docker镜像使用的是伪集群的方式，运行生成的数据放在宿主机/data/dockerData 目录里面


#常用命令：

使用方法：

进入dockerfile目录  

//编译并以后台方式运行  
docker-compose build && docker-compose up -d  

常用操作命令：

//查看当前镜像的docker进程
docker-compose ps

//启动容器以后台方式运行
docker-compose up -d

//停止当前镜像的docker进程
docker-compose stop

//卸载当前镜像和容器
docker-compose down

//执行容器内的zookeeper 客户端
docker exec  -it zookeeper-cluster-dev   /opt/zookeeper/bin/zkCli.sh

//进入容器名为zookeeper-cluster-dev的容器
docker exec  -it zookeeper-cluster-dev    /bin/bash

//查看所有的容器，包括已经停止的。
docker ps -a

//删除容器
docker rm <容器名orID>

//删除所有容器
docker rm $(docker ps -a -q)

//查看所有镜像
docker images -a 

//删除none镜像
docker rmi $(docker images | grep none | awk '{print $3}' | sort -r)

//删除所有镜像
docker rmi $(docker images | awk '{print $3}' | sort -r)

//查询未在使用的数据卷
docker volume ls -qf dangling=true

//查询未在使用的数据卷并删除
docker volume rm $(docker volume ls -qf dangling=true)


















