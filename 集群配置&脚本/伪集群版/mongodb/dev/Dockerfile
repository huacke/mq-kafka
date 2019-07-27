FROM mamohr/centos-java 

MAINTAINER walton <java01@walton.com>

ARG mongodb_version=4.0.10

ENV MONGODB_HOME=/usr/local/mongodb \
	JAVA_HOME=/usr/

ENV PATH ${PATH}:${MONGODB_HOME}/bin
ENV PATH $PATH:$HOME/bin:/sbin:/usr/bin:/usr/sbin

RUN wget -O /etc/yum.repos.d/epel.repo http://mirrors.aliyun.com/repo/epel-7.repo
RUN mv /etc/yum.repos.d/epel.repo /etc/yum.repos.d/epel.repo.backup
RUN yum clean all &&  yum makecache
RUN yum -y install  epel-release
RUN yum -y install  axel

COPY    axelget.conf /etc/yum/pluginconf.d/
COPY     axelget.py  /usr/lib/yum-plugins/


RUN yum -y install   wget openssl-devel   lrzsz 

RUN  wget -qO ./mongodb-linux-x86_64-rhel70-${mongodb_version}.tgz https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel70-${mongodb_version}.tgz && tar xfz mongodb-linux-x86_64-rhel70-${mongodb_version}.tgz  -C /opt

RUN mv opt/mongodb-linux-x86_64-rhel70-${mongodb_version} ${MONGODB_HOME}

RUN  mkdir -p /data/mongodb/node{1,2,3}/shard{1,2,3}/{db,logs}

RUN  mkdir -p /data/mongodb/node{1,2,3}/configsrv

COPY keyfile /data/mongodb/

COPY  conf /usr/local/mongodb/conf/


RUN    chmod 400 /data/mongodb/keyfile

COPY mongondbService.sh ${MONGODB_HOME}

#RUN sudo /usr/sbin/ntpdate cn.pool.ntp.org && sudo hwclock --systohc

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone
	
#RUN sysctl -w vm.zone_reclaim_mode=0 && 
#RUN echo never > /sys/kernel/mm/transparent_hugepage/enabled && echo never > /sys/kernel/mm/transparent_hugepage/defrag 

RUN echo -e " root hard nofile 65536 \n root soft nofile 65536 \n root hard memlock unlimited \n root hard nproc 2048 \n root hard as unlimited \n"  >>/etc/security/limits.conf	
	
RUN ulimit -Hn
	
RUN chmod 755 /usr/local/mongodb/mongondbService.sh && chmod 755 /data


VOLUME ["/data/mongodb"]

EXPOSE 23001 23002 23003

ENTRYPOINT ["/usr/local/mongodb/mongondbService.sh"]
	
