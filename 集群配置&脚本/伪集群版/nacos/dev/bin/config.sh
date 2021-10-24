#!/bin/bash

. /etc/profile
. ~/.bash_profile

cat <<- EOF | tee $CLUSTER_CONF
${NODE1_IP}:${NODE1_PORT}
${NODE2_IP}:${NODE2_PORT}
${NODE3_IP}:${NODE3_PORT}
EOF

#NODE1
sed -i '/^server.port=/c 'server.port=${NODE1_PORT}''   ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.url.0=/c '$(echo -e db.url.0=jdbc:mysql://${MYSQL_MASTER_SERVICE_HOST}:${MYSQL_MASTER_SERVICE_PORT}/${MYSQL_MASTER_SERVICE_DB_NAME}?characterEncoding=utf8\&connectTimeout=1000\&socketTimeout=3000\&autoReconnect=true)'' ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.user=/c 'db.user=${MYSQL_MASTER_SERVICE_USER}''   ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.password=/c 'db.password=${MYSQL_MASTER_SERVICE_PASSWORD}''   ${BASE_NACOS_HOME}/conf/application.properties

cp ${BASE_NACOS_HOME}/conf/application.properties  ${BASE_NACOS_HOME}/node1/conf/
cp ${BASE_NACOS_HOME}/conf/custom.properties  ${BASE_NACOS_HOME}/node1/conf/
cp ${BASE_NACOS_HOME}/conf/cluster.conf  ${BASE_NACOS_HOME}/node1/conf/

#NODE2
sed -i '/^server.port=/c 'server.port=${NODE2_PORT}''   ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.url.0=/c '$(echo -e db.url.0=jdbc:mysql://${MYSQL_MASTER_SERVICE_HOST}:${MYSQL_MASTER_SERVICE_PORT}/${MYSQL_MASTER_SERVICE_DB_NAME}?characterEncoding=utf8\&connectTimeout=1000\&socketTimeout=3000\&autoReconnect=true)'' ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.user=/c 'db.user=${MYSQL_MASTER_SERVICE_USER}''   ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.password=/c 'db.password=${MYSQL_MASTER_SERVICE_PASSWORD}''   ${BASE_NACOS_HOME}/conf/application.properties


cp ${BASE_NACOS_HOME}/conf/application.properties  ${BASE_NACOS_HOME}/node2/conf/
cp ${BASE_NACOS_HOME}/conf/custom.properties  ${BASE_NACOS_HOME}/node2/conf/
cp ${BASE_NACOS_HOME}/conf/cluster.conf  ${BASE_NACOS_HOME}/node2/conf/

#NODE3
sed -i '/^server.port=/c 'server.port=${NODE3_PORT}''   ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.url.0=/c '$(echo -e db.url.0=jdbc:mysql://${MYSQL_MASTER_SERVICE_HOST}:${MYSQL_MASTER_SERVICE_PORT}/${MYSQL_MASTER_SERVICE_DB_NAME}?characterEncoding=utf8\&connectTimeout=1000\&socketTimeout=3000\&autoReconnect=true)'' ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.user=/c 'db.user=${MYSQL_MASTER_SERVICE_USER}''   ${BASE_NACOS_HOME}/conf/application.properties
sed -i '/^db.password=/c 'db.password=${MYSQL_MASTER_SERVICE_PASSWORD}''   ${BASE_NACOS_HOME}/conf/application.properties

cp ${BASE_NACOS_HOME}/conf/application.properties  ${BASE_NACOS_HOME}/node3/conf/
cp ${BASE_NACOS_HOME}/conf/custom.properties  ${BASE_NACOS_HOME}/node3/conf/
cp ${BASE_NACOS_HOME}/conf/cluster.conf  ${BASE_NACOS_HOME}/node3/conf/
