
进入容器
docker exec  -it gitlab /bin/bash


//迁移git代码 方式1（推荐）
1，备份（原机器）
进入容器
docker exec  -it gitlab /bin/bash
#进入备份目录
cd /var/opt/gitlab/backups
#备份gitlab
gitlab-rake gitlab:backup:create
#复制备份数据到宿主机
docker cp c9132c59fa02:/var/opt/gitlab/backups/1834251675_2019_12_17_11.1.4_gitlab_backup.tar /root/

2，恢复（目标机器）
#复制备份数据到容器
docker cp 1834251675_2019_12_17_11.1.4_gitlab_backup.tar  c8132c59fa02:/var/opt/gitlab/backups
#进入备份目录
cd /var/opt/gitlab/backups
#授权
chmod -R 755 1834251675_2019_12_17_11.1.4_gitlab_backup.tar
#恢复备份
gitlab-rake gitlab:backup:restore BACKUP=1834251675_2019_12_17_11.1.4


//迁移git代码 方式2（裸库迁移）
1,进入容器,chown -R git:git /var/opt/gitlab/git-data/repositories
2,把原代码仓库 /var/opt/gitlab/git-data/repositories 文件夹中的目录 导入到宿主机 /data/dockerData/gitlab/repositories 目录中
3,授权 chown -R git:git /var/opt/gitlab/git-data/repositories
4,执行 gitlab-rake gitlab:import:repos['/var/opt/gitlab/git-data/repositories']





