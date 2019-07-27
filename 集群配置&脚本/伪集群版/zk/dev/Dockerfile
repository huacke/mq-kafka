FROM mamohr/centos-java 

MAINTAINER walton <java01@walton.com>

ENV ZOOKEEPER_VERSION 3.5.5

RUN yum -y install tar wget

RUN wget -q -O - http://apache.mirrors.pair.com/zookeeper/zookeeper-$ZOOKEEPER_VERSION/apache-zookeeper-$ZOOKEEPER_VERSION-bin.tar.gz | tar -xzf - -C /opt \
    && mv /opt/apache-zookeeper-$ZOOKEEPER_VERSION-bin /opt/zookeeper \
    && mkdir -p /tmp/zookeeper

COPY  conf/* /opt/zookeeper/conf/

COPY  zkService.sh /opt/zookeeper/

RUN mkdir -p /data/zk/node1
RUN mkdir -p /data/zk/node2
RUN mkdir -p /data/zk/node3
RUN mkdir -p /data/zk/node1/logs
RUN mkdir -p /data/zk/node2/logs
RUN mkdir -p /data/zk/node3/logs
RUN touch  /data/zk/node1/myid  && echo 0 > /data/zk/node1/myid
RUN touch  /data/zk/node2/myid  && echo 1 > /data/zk/node2/myid
RUN touch  /data/zk/node3/myid  && echo 2 > /data/zk/node3/myid

WORKDIR /opt/zookeeper

ENV JAVA_HOME /usr/bin/java

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

EXPOSE 2181 2182 2183

RUN chmod 755 /opt/zookeeper/zkService.sh
RUN chmod 755 /data

VOLUME ["/data/zk"]

ENTRYPOINT ["/opt/zookeeper/zkService.sh"]
	
