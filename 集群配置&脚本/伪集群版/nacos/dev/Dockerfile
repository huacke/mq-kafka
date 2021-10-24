FROM mamohr/centos-java

MAINTAINER huacke "huacke@163.com"

ARG NACOS_VERSION=1.1.4
ARG base_home=/usr/local

ENV MODE="cluster" \
    PREFER_HOST_MODE="ip"\
    BASE_HOME="${base_home}" \
    BASE_NACOS_HOME=${base_home}/nacos \
    FUNCTION_MODE="all" \
    JAVA_HOME="/usr/java/jdk1.8.0_162" \
    NACOS_USER="nacos" \
    JAVA="$JAVA_HOME/bin/java" \
    JVM_XMS="2g" \
    JVM_XMX="2g" \
    JVM_XMN="1g" \
    JVM_MS="128m" \
    JVM_MMS="320m" \
    NACOS_DEBUG="n" \
    TOMCAT_ACCESSLOG_ENABLED="false" \
    TIME_ZONE="Asia/Shanghai"



COPY nacos-server-${NACOS_VERSION}.tar.gz $BASE_HOME


RUN  yum clean all\
    && yum install -y wget iputils nc  vim libcurl\
    && tar -xzvf $BASE_HOME/nacos-server-${NACOS_VERSION}.tar.gz -C $BASE_HOME \
    && rm -rf $BASE_HOME/nacos-server-${NACOS_VERSION}.tar.gz ${BASE_NACOS_HOME}/bin/* ${BASE_NACOS_HOME}/conf/*.properties ${BASE_NACOS_HOME}/conf/*.example ${BASE_NACOS_HOME}/conf/*.xml  ${BASE_NACOS_HOME}/conf/nacos-mysql.sql \
    && ln -snf /usr/share/zoneinfo/$TIME_ZONE /etc/localtime && echo '$TIME_ZONE' > /etc/timezone \
	&& mkdir -p ${BASE_NACOS_HOME}/node{1,2,3}/{conf,logs}

COPY bin/startSrv.sh  ${BASE_NACOS_HOME}/bin/
COPY bin/startup.sh /opt/
COPY bin/config.sh ${BASE_NACOS_HOME}/bin/

COPY conf/cluster.conf ${BASE_NACOS_HOME}/conf/
COPY conf/custom.properties ${BASE_NACOS_HOME}/conf/
COPY conf/application.properties ${BASE_NACOS_HOME}/conf/
COPY conf/nacos-logback.xml  ${BASE_NACOS_HOME}/conf/

RUN mkdir -p ${BASE_NACOS_HOME}/logs \
	&& touch ${BASE_NACOS_HOME}/logs/start.out \
	&& ln -sf /dev/stdout ${BASE_NACOS_HOME}/logs/start.out \
	&& ln -sf /dev/stderr ${BASE_NACOS_HOME}/logs/start.out
	
RUN chmod +x ${BASE_NACOS_HOME}/bin/startSrv.sh && chmod +x /opt/startup.sh && chmod +x ${BASE_NACOS_HOME}/bin/config.sh

EXPOSE 8848 8849 8850

ENTRYPOINT ["/opt/startup.sh"]
