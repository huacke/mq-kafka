启动方式：

分别在三台机器上执行

mv zookeeper.service  /etc/systemd/system

systemctl daemon-reload

systemctl enable  zookeeper.service

service zookeeper start
