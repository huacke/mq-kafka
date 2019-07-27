#! /bin/bash
# chkconfig:   2345 90 10

##################################################################
#   FileName startkafkaEagle.sh
#   Description start kafkaEagle  service
#################################################################

KAFKA_EAGLE_HOME=/usr/local/kafka/kafka-eagle-web-1.3.3

KAFKA_EAGLE_BIN_HOME=${KAFKA_EAGLE_HOME}/bin

KAFKA_EAGLE_LOG_PATH=${KAFKA_EAGLE_HOME}/logs

KAFKA_EAGLE_BIN=${KAFKA_EAGLE_BIN_HOME}/ke.sh

export KE_HOME=${KAFKA_EAGLE_HOME}
source /etc/profile


start()
{

echo -e "Starting  kafkaEagle  service    ....."

exec ${KAFKA_EAGLE_BIN}  start  >>${KAFKA_EAGLE_LOG_PATH}/kafkaEagle.log

echo -e "Starting kafkaEagle service  done"

}

stop()

{
  ${KAFKA_EAGLE_BIN}  stop

}


restart()

{
    stop
    start
}

status()

{

  ${KAFKA_EAGLE_BIN}  status
  exit 0 

}


stats(){
  ${KAFKA_EAGLE_BIN} stats
  exit 0 
}


usage()

{

 echo -e "Usage: $0 [start|stop|restart|status|stats | usage]"

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

stats)

stats

;;

*)

usage

;;

esac
