#!/bin/bash

#docker-ce
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

sudo yum install -y yum-utils device-mapper-persistent-data lvm2 && sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo && sudo yum install -y docker-ce docker-ce-cli containerd.io

#docker-compose(�����������е��������ܻ����ʧ�ܵ��������Ҫ���Լ���)
sudo curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && sudo chmod +x /usr/local/bin/docker-compose && sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

#docker�־û����(�����������е��������ܻ����ʧ�ܵ��������Ҫ���Լ���)
curl -fsSL https://raw.githubusercontent.com/CWSpear/local-persist/master/scripts/install.sh | sudo bash


#docker�������

cat <<- EOF | tee /etc/docker/daemon.json 
{
    "registry-mirrors": ["https://72idtxd8.mirror.aliyuncs.com","https://docker.mirrors.ustc.edu.cn"]
}
EOF

#����docker
systemctl daemon-reload && systemctl restart docker

