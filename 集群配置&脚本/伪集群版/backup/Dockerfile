FROM mamohr/centos-java 

MAINTAINER huacke <huacke@163.com>

ARG back_up_home=/data/backup

ENV BACK_UP_HOME=$back_up_home \
    CRON_LOG_FILE=$back_up_home/cron.log \
    JAVA_HOME=/usr	
	
ENV PATH $PATH:$HOME/bin:/sbin:/usr/bin:/usr/sbin

RUN yum -y install tar wget  expect rsync openssh openssh-clients crontabs cronie  sshpass

RUN mkdir -p ${BACK_UP_HOME}

COPY mysqldump.sh auto_ssh_.sh git-data-dump.sh jenkins-data-dump.sh maven-data-dump.sh runbackup.sh crontab.conf /opt/

RUN chmod +x /opt/*.sh

#RUN sed -i '/StrictHostKeyChecking/c StrictHostKeyChecking no' /etc/ssh/ssh_config

RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa

RUN echo "export LANG=$LANG" > /etc/profile.d/locale.sh \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

#RUN sed -i '/session    required   pam_loginuid.so/c\#session    required   pam_loginuid.so' /etc/pam.d/crond

RUN cat /opt/crontab.conf  >> /var/spool/cron/root

VOLUME ["${BACK_UP_HOME}"]

ENTRYPOINT ["/opt/runbackup.sh"]

#ENTRYPOINT ["/opt/mysqldump.sh"]
