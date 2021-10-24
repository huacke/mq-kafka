
//初始化密码查看
cat /data/dockerData/jenkins/secrets/initialAdminPassword

//备份下载源配置
cp /data/dockerData/jenkins/updates/default.json /data/dockerData/jenkins/updates/default.json.bak

//替换下载源加速
sed -i 's/http:\/\/updates.jenkins-ci.org\/download/https:\/\/mirrors.tuna.tsinghua.edu.cn\/jenkins/g' /data/dockerData/jenkins/updates/default.json && sed -i 's/http:\/\/www.google.com/https:\/\/www.baidu.com/g' /data/dockerData/jenkins/updates/default.json

//替换更新插件源
https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json



//常用命令：
进入容器
docker exec  -it jenkins /bin/bash
