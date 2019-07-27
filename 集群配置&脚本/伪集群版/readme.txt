目前docker镜像使用的是伪集群的方式，运行生成的数据放在宿主机/data/dockerData 目录里面
使用前需要安装一下组件：

1,docker-ce


sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

sudo yum install -y yum-utils device-mapper-persistent-data lvm2

sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

sudo yum install docker-ce docker-ce-cli containerd.io




2,docker-docker-compose

sudo curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

sudo chmod +x /usr/local/bin/docker-compose

sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose


3,local persist volume Plugin for docker


curl -fsSL https://raw.githubusercontent.com/CWSpear/local-persist/master/scripts/install.sh | sudo bash


使用方法：

进入dockerfile目录  

docker-compose build && docker-compose up -d  //编译并以后台方式运行  


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

//进入容器
docker exec  -it zookeeper-cluster-dev    /bin/bash


//查看所有的容器，包括已经停止的。
docker ps -a

//删除容器
docker rm <容器名orID>

//删除所有容器
docker rm $(docker ps -a -q)


//查看所有镜像
docker images -a 


//删除所有镜像
docker rmi $(docker images | grep none | awk '{print $3}' | sort -r)
docker rmi $(docker images | awk '{print $3}' | sort -r)


//查询未在使用的数据卷
docker volume ls -qf dangling=true

//查询未在使用的数据卷并删除
docker volume rm $(docker volume ls -qf dangling=true)


















