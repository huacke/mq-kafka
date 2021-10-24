#!/bin/sh -e

FILENAME="kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz"

SCALA_FILENAME="scala-${SCALA_DOWNLOAD_VERSION}.tgz"

url=https://mirrors.tuna.tsinghua.edu.cn/apache/kafka/${KAFKA_VERSION}/${FILENAME}

scala_url=https://downloads.lightbend.com/scala/${SCALA_DOWNLOAD_VERSION}/${SCALA_FILENAME}

echo "Downloading Kafka from $url"

wget  -qO /tmp/${FILENAME}  ${url}

echo "Downloading scala from $scala_url"

wget  -qO /tmp/${SCALA_FILENAME}  ${scala_url}