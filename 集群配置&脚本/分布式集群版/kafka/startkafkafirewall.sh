#! /bin/bash
# chkconfig:   2345 90 10

##################################################################
#   FileName startkafkafirewall.sh
#   Description start kafkafirewall  service
#   使用JMX监控kafka，kafka jmx 启动时会有多个端口，因为它每次启动时除了固定的一个jmx端口，还会会随机分配几个端口
#   需要把这几个随机端口也开放
#################################################################

start()
{
echo -e "Starting kafka firewall ......."
sleep 10
addFirewallPort
echo -e "Started kafka firewall"

}


addFirewallPort()
{

  R_PIDS=$(ps -elf | grep  'kafka.Kafka' |grep -v grep |awk '{print $4}'|xargs)
     if [ "$R_PIDS" != "" ];then
       for index in ${!R_PIDS[@]}
        do   
             currentPort=${R_PIDS[$index]}
	   N_PORTS=$(netstat -antlp | grep ${currentPort} | awk  '{print $4}'|awk -F[:]  '{print $2}'|uniq -u|xargs)
	     if [ "$N_PORTS" != "" ];then
                   N_PORTS=($N_PORTS)
		  firewall-cmd --reload
		for pindex in ${!N_PORTS[@]}
		    do
		       firewall-cmd --zone=public --add-port=${N_PORTS[$pindex]}/tcp
		    done
	     fi
        done
    fi
}


stop()

{

exit 1

}

restart()
{
    exit 0 
}

status()

{

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
