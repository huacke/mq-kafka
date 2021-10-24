FROM  mamohr/centos-java 

MAINTAINER huacke <huacke@163.com>

ARG maven_download_version=3.6.3
ARG jenkins_file_name=jenkins-2.121.2.war
ARG node_version=12.14.0
ARG node_file_name=node-v${node_version}-linux-x64.tar.xz
ARG maven_file_name=apache-maven-${maven_download_version}-bin.tar.gz

ENV MAVEN_FILE_NAME=$maven_file_name \
    NODE_VERSION=$node_version \
    NODE_FILENAME=$node_file_name \
    NODE_HOME=/opt/node \
    MAVEN_VERSION=$maven_download_version \
    JENKINS_FILE_NAME=$jenkins_file_name \
    JENKINS_HOME=/data/jenkins \
    JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8 \
    MAVEN_HOME=/opt/maven
	
ENV JAVA_HOME=/usr/java/jdk1.8.0_162
ENV PATH ${PATH}:${JAVA_HOME}/bin
ENV PATH ${PATH}:${MAVEN_HOME}/bin
ENV PATH=${PATH}:${NODE_HOME}/bin

ENV PATH $PATH:$HOME/bin:/sbin:/usr/bin:/usr/sbin

RUN mkdir -p /data/jenkins

RUN mkdir -p /data/maven-repo

COPY ${JENKINS_FILE_NAME}  /opt/jenkins.war

COPY jenkinsService.sh maven-download.sh node-download.sh  /opt/

RUN chmod +x /opt/*.sh && chmod 755 /data

RUN yum -y install tar wget git

RUN sh /opt/maven-download.sh && tar xfz /tmp/${MAVEN_FILE_NAME} -C /opt && rm /tmp/${MAVEN_FILE_NAME}
RUN sh /opt/node-download.sh && tar xvf /tmp/${NODE_FILENAME} -C /opt && rm /tmp/${NODE_FILENAME}

RUN mv opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME}

RUN mv opt/node-v${NODE_VERSION}-linux-x64 ${NODE_HOME}

RUN  rm -rf  ${MAVEN_HOME}/conf/settings.xml

COPY settings.xml  ${MAVEN_HOME}/conf

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

VOLUME ["/data"]

EXPOSE 8080

ENTRYPOINT ["/opt/jenkinsService.sh"]
	
