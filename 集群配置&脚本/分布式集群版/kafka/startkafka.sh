#! /bin/bash
# chkconfig:   2345 90 10

##################################################################
#   FileName startkafka.sh
#   Description start kafka  service
#################################################################

start()
{
KAFKA_HOME=/usr/local/kafka/kafka_2.12-2.3.0

KAFKA_BIN_HOME=${KAFKA_HOME}/bin

KAFKA_BIN=${KAFKA_BIN_HOME}/kafka-server-start.sh

KAFKA_CONFIG_HOME=${KAFKA_HOME}/config

KAFKA_LOG_PATH=/data/kafka/logs

export LOG_DIR=${KAFKA_LOG_PATH}

export JMX_PORT=19999

source /etc/profile

export KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote=true -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=172.19.36.76"

echo -e "Starting  kafka  service    ....."

sh ${KAFKA_BIN} -daemon  ${KAFKA_CONFIG_HOME}/server.properties

sh /usr/local/kafka/startkafkafirewall.sh start &

echo -e "Starting kafka service  done"

}



stop()

{

PIDS=$(ps ax | grep -i 'kafka.Kafka' | grep -v grep | awk '{print $1}')

if [ "$PIDS" == "" ]

then

   echo -e "NO such process found!"
   firewall-cmd --reload
   exit 1

fi

echo -e "Stopping kafka  service  :{$PIDS} ...."

kill -15 ${PIDS}

firewall-cmd --reload

exit 

}

restart()
{
    echo "INFO : kafka  service is stoping ... "
    stop
    echo "INFO : kafka  service is starting ..."
    start
}

status()

{

  Z_PIDS=$(ps -elf | grep -i 'kafka.Kafka' |grep -v grep |awk '{print $4}' |xargs )

  if [ "$C_PIDS" == "" ]

  then

 Z_STATUS="NOT running"

  else

 Z_STATUS="Running"

  fi

  echo -e "kafka nodes:{$Z_PIDS}:{${Z_STATUS}}"

  exit 0 

}

usage()

{

 echo -e "Usage: $0 [start|restart|stop|status]"

 exit 1

}

case  "$1" in

start)

start

;;

stop)

stop

;;

restart)

restart

;;

status)

status

;;

*)

usage

;;

esac
