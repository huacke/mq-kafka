启动方式：

分别在三台机器上执行


mv mongodb.service  /etc/systemd/system

systemctl daemon-reload

systemctl enable  mongodb.service

service mongodb start



