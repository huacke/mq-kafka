FROM mamohr/centos-java 

MAINTAINER huacke <huacke@163.com>

ARG dist_dir=mysql-5.7.21-linux-glibc2.12-x86_64
ARG filename=${dist_dir}.tar.gz
ARG mysql_home=/usr/local/mysql



ENV FILENAME=$filename \
        DIST_DIR=${dist_dir} \
	JAVA_HOME=/usr \	
	MYSQL_HOME=$mysql_home
		
ENV PATH ${PATH}:${MYSQL_HOME}/bin
	
ENV PATH $PATH:$HOME/bin:/sbin:/usr/bin:/usr/sbin
 
 
COPY  $FILENAME /tmp/

RUN yum -y install tar wget libaio numactl openssl

RUN tar xfz /tmp/$FILENAME -C /opt && rm -rf /tmp/$FILENAME

RUN mv opt/$DIST_DIR  ${MYSQL_HOME}

RUN mkdir -p ${MYSQL_HOME}/{data,log,etc,run} 

RUN touch ${MYSQL_HOME}/run/mysqld.log

RUN  chmod 750 ${MYSQL_HOME}/{data,log,etc,run}

COPY my.cnf  ${MYSQL_HOME}/etc

RUN touch ${MYSQL_HOME}/log/mysqld.log

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

RUN groupadd mysql && useradd -r -g mysql -s /bin/false mysql

RUN chown -R mysql:mysql ${MYSQL_HOME}

RUN  mysqld --initialize --user=mysql --basedir=${MYSQL_HOME} --datadir=${MYSQL_HOME}/data

VOLUME ["${MYSQL_HOME}"]

EXPOSE 3306

COPY mysqlService.sh /opt/

RUN chmod +x /opt/mysqlService.sh


ENTRYPOINT ["/opt/mysqlService.sh"]
	
