#!/bin/bash

# chkconfig:   2345 90 10
##################################################################

#   FileName startcluster.sh

#   Description start redis cluster  service

#################################################################

. /etc/profile

. ~/.bash_profile


SHELL_REDIS=/usr/local/redis/script/startRedis.sh

SHELL_SENTINEL=/usr/local/redis/script/startSentinel.sh



start()

{



sh $SHELL_REDIS start

sh $SHELL_SENTINEL start



}



stop()



{



sh $SHELL_REDIS stop

sh $SHELL_SENTINEL stop



}



restart()

{

    stop

    start

}





usage()



{



 echo -e "Usage: $0 [start|restart|stop]"



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



*)



usage



;;



esac

