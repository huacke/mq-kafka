#!/bin/bash

#安装核心软件
yum install yum-plugin-fastestmirror -y &&\ 
yum groupinstall   "Development tools" -y  &&\
yum install -y epel-release  ntp ntpdate lrzsz bash-completion vim  wget telnet perl perl-devel openssl openssl-devel net-tools &&\
yum install -y gdb make  cmake  kernel-devel  tig git-svn gcc gcc-c++ gcc-gfortran compat-gcc-44 compat-gcc-44-c++ &&\
yum install -y compat-gcc-44-gfortran compat-libf2c-34  ntfs-3g p7zip unrar zsh autojump autojump-zsh  nfs-utils &&\ 
yum install -y dos2unix unix2dos meld   links  axel  net-tools xorg-x11-fonts-75dpi xorg-x11-fonts-Type1 


#设置主机名
echo "centos" > /etc/hostname


#配置时区
cat <<- EOF | tee /etc/sysconfig/clock 

ZONE=Asia/Shanghai

UTC=false

ARC=false

EOF



#时间同步
ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && ntpdate cn.pool.ntp.org && /sbin/hwclock --systohc

  
#关闭selinux 
setenforce 0 &&  sed -i '/^SELINUX=/c SELINUX=disabled' /etc/selinux/config




####防火墙

#临时关闭
#service iptables stop
#禁止开机启动
#chkconfig iptables off


#firewalld的基本使用
#启动 
#systemctl start firewalld
#关闭 
#systemctl stop firewalld
#查看状态 
#systemctl status firewalld 
#开机禁用 
#systemctl disable firewalld
#开机启动
#systemctl enable firewalld

# 查询端口是否开放
#firewall-cmd --query-port=8080/tcp
# 开放端口
#firewall-cmd --permanent --add-port=80/tcp
#firewall-cmd --permanent --add-port=8080-8085/tcp
#移除端口
#firewall-cmd --permanent --remove-port=8080/tcp

#防火墙配置重载（更新了配置必须重载后才能生效）
#firewall-cmd --reload

