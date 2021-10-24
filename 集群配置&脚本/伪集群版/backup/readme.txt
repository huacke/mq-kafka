#定时任务备份数据

//常用命令：
进入容器

docker exec  -it backup /bin/bash

注： 


1，对应配置在目录 *.env 文件中，eg: git-data-dump.env

2，定时任务在crontab.conf中配置

3，配置文件中的用户名和密码中有特殊字符 例如 @ $ # 等等需要 用 \ 转义 ,不然远程无验证登录会出问题，导致无法备份。
eg ： 密码 huacke@2020   =>  huacke\@2020


