#!/bin/bash



. /etc/profile

. ~/.bash_profile



RPORTS=(${NODE1_PORT} ${NODE2_PORT} ${NODE3_PORT})

BASE_HONME=/usr/local/redis

EXEC=/usr/local/bin/redis-server

CLIEXEC=/usr/local/bin/redis-cli

PWD="${NODE1_REQUIREPASS}"



start()

{



echo -e "Starting  redis-server nodes {${RPORTS[@]}} ....."



CPORTS=($(ps ax | grep -i 'redis-server' |grep -v 'sentinel' | grep -v grep | awk '{print $6}' | awk -F ':'  '{print $2}' | xargs))





for index in ${!RPORTS[@]}



do





if echo "${CPORTS[@]}" | grep -w "${RPORTS[$index]}" &>/dev/null; then

    echo " redis-server port ${RPORTS[$index]}  process is already running ingroe ..."

    continue

else





if [ ! -d  ${BASE_HONME}/data/${RPORTS[$index]} ]; then



 mkdir -p  ${BASE_HONME}/data/${RPORTS[$index]}



fi



CONF="${BASE_HONME}/conf/${RPORTS[$index]}.conf"



echo -e "Starting redis-server port: ${RPORTS[$index]} ............ "



$EXEC $CONF



fi



done



}



stop()



{



echo -e "Stopping redis-server all :${RPORTS[@]} ...."



for index in ${!RPORTS[@]}

do

   

   PID=$(ps ax | grep -i 'redis-server' |grep -v 'sentinel' | grep -i ${RPORTS[$index]} | grep -v grep | awk '{print $1}' | xargs)

   if [ "$PID" == "" ];then

   echo -e "NO such port ${RPORTS[$index]} process found! ingore ...." 

   else

   echo -e "Stopping redis-server :${RPORTS[$index]} ...."   

   $CLIEXEC -a $PWD  -p ${RPORTS[$index]} shutdown

    while [ -x /proc/${PID[0]} ]

       do

          echo -e "Waiting for redis server prot: ${RPORTS[$index]}  to shutdown ..."

          sleep 1

       done

    echo -e "Stopping redis-server :${RPORTS[$index]} done"

   fi   

done





}



restart()

{

    stop

    start

    exit 1

}





status()



{



  R_PIDS=$(ps ax | grep -i 'redis-server' |grep -v 'sentinel' | grep -v grep | awk '{print $1}' | xargs )



  echo -e "redis-server all nodes:{${RPORTS[@]}}: running nodes pid is {${R_PIDS}}"



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

