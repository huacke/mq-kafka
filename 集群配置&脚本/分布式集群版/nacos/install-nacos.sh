#!/bin/bash

. /etc/profile

. ~/.bash_profile

#当前机器ip
NODE_IP=$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | awk -F"/" '{print $1}')

#nacos安装版本
export NACOS_VERSION=1.1.4
#axel安装版本
export AXEL_VERSION=2.17.7
#基础目录
export BASE_INSTALL_DIR=/usr/local
#nacos安装目录
export NACOS_INSTALL_DIR=/usr/local/nacos
#nacos配置目录
export NACOS_CONF_DIR=${NACOS_INSTALL_DIR}/conf
#axel安装目录
export AXEL_INSTALL_DIR=/usr/local/axel
#mysql地址链接
export DB_URL="jdbc:mysql://192.168.0.109:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true"
#数据库用户名
export DB_USER=root
#数据库用户密码
export DB_PASSWD=root
#nacos端口
export NACOS_PORT=8848
#集群节点
export CLUSTER_NODES=(192.168.0.109 192.168.0.111 192.168.0.100) 

#卸载旧版本axel，使用最新源码安装
yum remove axel -y

#如报错,安装开发工具
#yum groupinstall "Development tools"

#github 加速
cat <<- EOF | tee -a /etc/hosts

192.30.253.113 github.com
151.101.25.194 github.global.ssl.fastly.net
192.30.253.121 codeload.github.com

EOF


#安装axel & nacos(github 下载速度很不稳定，如果安装进度无反应，请重新执行安装脚本)
cd /tmp && rm -rf axel* nacos*  && wget https://github.com/axel-download-accelerator/axel/releases/download/v${AXEL_VERSION}/axel-${AXEL_VERSION}.tar.gz && tar -xzf axel-${AXEL_VERSION}.tar.gz  -C /tmp && rm -rf ${AXEL_INSTALL_DIR} &&  mv /tmp/axel-${AXEL_VERSION} ${AXEL_INSTALL_DIR} && cd ${AXEL_INSTALL_DIR} && ./configure --prefix=${AXEL_INSTALL_DIR} && make && make install && rm -rf /tmp/axel*  && \
echo "PATH="''${AXEL_INSTALL_DIR}''"/bin:\$PATH" > /etc/profile.d/axel.sh && source /etc/profile  &&\
axel -a -n 50 https://github.com/alibaba/nacos/releases/download/${NACOS_VERSION}/nacos-server-${NACOS_VERSION}.tar.gz  && \
cd /tmp  && tar -xzf nacos-server-${NACOS_VERSION}.tar.gz   -C /tmp && mv /tmp/nacos nacos-server-${NACOS_VERSION} && rm -rf ${NACOS_INSTALL_DIR} && mv nacos-server-${NACOS_VERSION}  ${NACOS_INSTALL_DIR} &&  rm -rf /tmp/nacos*

rm -rf $NACOS_INSTALL_DIR/bin/{shutdown.cmd,startup.cmd}

#启动脚本加入配置
sed '15 a set -x'  -i  ${NACOS_INSTALL_DIR}/bin/startup.sh
sed '16 a  . /etc/profile'  -i  ${NACOS_INSTALL_DIR}/bin/startup.sh
sed '17 a  . ~/.bash_profile'  -i  ${NACOS_INSTALL_DIR}/bin/startup.sh
sed '18 a  '"NC_SERVER_IP=\$(ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print \$2}' | awk -F"/" '{print \$1}')"''  -i  ${NACOS_INSTALL_DIR}/bin/startup.sh
sed '19 a  NACOS_SERVER_IP=$NC_SERVER_IP'  -i  ${NACOS_INSTALL_DIR}/bin/startup.sh

#配置生成
cat <<- EOF | tee ${NACOS_CONF_DIR}/application.properties

server.contextPath=/nacos
server.servlet.contextPath=/nacos
server.port=${NACOS_PORT}
db.num=1
db.url.0=${DB_URL}
db.user=${DB_USER}
db.password=${DB_PASSWD}
management.security=false
spring.security.enabled=false
security.basic.enabled=false
nacos.security.ignore.urls=/,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-fe/public/**,/v1/auth/login,/v1/console/health/**,/v1/cs/**,/v1/ns/**,/v1/cmdb/**,/actuator/**,/v1/console/server/**

EOF

#集群配置生成
cat <<- EOF | tee ${NACOS_CONF_DIR}/cluster.conf
$(
for index in ${!CLUSTER_NODES[@]}
do
echo ${CLUSTER_NODES[$index]}:${NACOS_PORT}
done
)
EOF

#nacos systemd 配置生成
cat <<- EOF | tee /etc/systemd/system/nacos.service

[Unit]

Description=nacos服务 

After=network.target

After=syslog.target

[Service]

Type=forking

ExecStart=${NACOS_INSTALL_DIR}/bin/startup.sh start

ExecStopPost=${NACOS_INSTALL_DIR}/bin/shutdown.sh stop

Restart=always

SuccessExitStatus=143

[Install]

WantedBy=multi-user.target

EOF

#重载systemd,配置生效
systemctl daemon-reload

#zk 集群开机启动
systemctl enable  nacos.service

#开放防火墙端口
firewall-cmd --permanent --zone=public --add-port=${NACOS_PORT}/tcp

firewall-cmd --reload