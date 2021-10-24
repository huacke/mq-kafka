#!/bin/sh -e

FILENAME="apache-maven-${MAVEN_VERSION}-bin.tar.gz"

url=https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/${MAVEN_VERSION}/binaries/${FILENAME}

echo "Downloading maven from $url"
wget  -qO /tmp/${FILENAME}  ${url}