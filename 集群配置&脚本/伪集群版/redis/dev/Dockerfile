FROM mamohr/centos-java 

MAINTAINER walton <java01@walton.com>

ARG reids_home=/usr/local/redis
ARG reids_version=5.0.5

ENV REIDS_FILE=redis-$reids_version \
    REIDS_HOME=$reids_home \
    JAVA_HOME=/usr	
	
ENV PATH $PATH:$HOME/bin:/sbin:/usr/bin:/usr/sbin

RUN yum -y install tar wget  expect  gcc  vim  automake autoconf libtool make

COPY ${REIDS_FILE}.tar.gz /tmp/ 

RUN tar xfz /tmp/${REIDS_FILE}.tar.gz -C /opt && rm /tmp/${REIDS_FILE}.tar.gz

RUN mv opt/${REIDS_FILE} ${REIDS_HOME} && mkdir -p ${REIDS_HOME}/{conf,data,logs}

RUN echo -e " root hard nofile 65536 \n root soft nofile 65536 \n root hard memlock unlimited \n root hard nproc 2048 \n root hard as unlimited \n"  >>/etc/security/limits.conf	

RUN cd ${REIDS_HOME} && make && make test && make install

COPY redis.conf sentinel.conf config.sh startup.sh /opt/


COPY script ${REIDS_HOME}/script/

RUN chmod +x /opt/*.sh && chmod +x ${REIDS_HOME}/script/*.sh

#COPY startup.sh /etc/init.d

#RUN  cd /etc/init.d/ && chmod +x  /etc/init.d/startup.sh && chkconfig --add  startup.sh  && chkconfig startup.sh  on

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

VOLUME ["${REIDS_HOME}"]

ENTRYPOINT ["/opt/startup.sh"]

