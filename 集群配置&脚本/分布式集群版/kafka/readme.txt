启动方式：

分别在三台机器上执行

mv kafka.service  /etc/systemd/system

systemctl daemon-reload

systemctl enable  kafka.service

service kafka start
