FROM mamohr/centos-java 

MAINTAINER walton <java01@walton.com>

ARG kafka_version=2.2.0
ARG scala_version=2.12
ARG scala_download_version=2.12.8


ENV KAFKA_VERSION=$kafka_version \
    SCALA_VERSION=$scala_version \
	SCALA_DOWNLOAD_VERSION=$scala_download_version \
    KAFKA_HOME=/opt/kafka \
	SCALA_HOME=/opt/scala \
	JAVA_HOME=/usr/

ENV PATH ${PATH}:${SCALA_HOME}/bin
ENV PATH ${PATH}:${KAFKA_HOME}/bin
ENV PATH $PATH:$HOME/bin:/sbin:/usr/bin:/usr/sbin


#passwd openssh-server openssh-clients initscripts
#RUN echo 'root:root' | chpasswd && /usr/sbin/sshd-keygen
#RUN sed -i '$a\vm.max_map_count=262144' /etc/sysctl.conf \


COPY download.sh /tmp/ 


RUN yum -y install tar wget 

RUN chmod +x /tmp/*.sh
RUN sh /tmp/download.sh && tar xfz /tmp/scala-${SCALA_DOWNLOAD_VERSION}.tgz -C /opt && rm /tmp/scala-${SCALA_DOWNLOAD_VERSION}.tgz
RUN mv opt/scala-${SCALA_DOWNLOAD_VERSION} ${SCALA_HOME}
RUN tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt  && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz
RUN mv opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} ${KAFKA_HOME}


COPY  conf/*  /opt/kafka/config/
RUN   touch   /opt/kafka/config/topic.properties

RUN sed '5 a export KAFKA_OPTS="-Xmx1G -Xms1G -Djava.security.auth.login.config=/opt/kafka/config/kafka_server_jaas.conf"'  -i  /opt/kafka/bin/kafka-server-start.sh
RUN sed '5 a export KAFKA_OPTS="-Djava.security.auth.login.config=/opt/kafka/config/kafka_client_jaas.conf"'  -i  /opt/kafka/bin/kafka-console-producer.sh
RUN sed '5 a export KAFKA_OPTS="-Djava.security.auth.login.config=/opt/kafka/config/kafka_client_jaas.conf"'  -i  /opt/kafka/bin/kafka-console-consumer.sh
RUN sed '5 a export KAFKA_OPTS="-Djava.security.auth.login.config=/opt/kafka/config/kafka_client_jaas.conf"'  -i  /opt/kafka/bin/kafka-topics.sh


RUN sed '6 a security.protocol=SASL_PLAINTEXT'  -i  /opt/kafka/config/producer.properties
RUN sed '6 a security.protocol=SASL_PLAINTEXT'  -i  /opt/kafka/config/consumer.properties
RUN echo "security.protocol=SASL_PLAINTEXT" >>  /opt/kafka/config/topic.properties

RUN sed '7 a sasl.mechanism=PLAIN'  -i  /opt/kafka/config/producer.properties
RUN sed '7 a sasl.mechanism=PLAIN'  -i  /opt/kafka/config/consumer.properties
RUN sed '1 a sasl.mechanism=PLAIN'  -i  /opt/kafka/config/topic.properties

  
COPY kafkaService.sh /opt/kafka 


RUN mkdir -p /data/kafka/node1/data && mkdir -p /data/kafka/node2/data && mkdir -p /data/kafka/node3/data 
RUN mkdir -p /data/kafka/node1/logs  && mkdir -p /data/kafka/node2/logs && mkdir -p /data/kafka/node3/logs
	
	
RUN echo -e " root hard nofile 65536 \n root soft nofile 65536 \n root hard memlock unlimited \n root hard nproc 2048 \n root hard as unlimited \n"  >>/etc/security/limits.conf

RUN ulimit -Hn
	

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone


RUN chmod 755 /opt/kafka/kafkaService.sh && chmod 755 /data

VOLUME ["/data"]

EXPOSE 19093 19094 19095

ENTRYPOINT ["/opt/kafka/kafkaService.sh"]
	
