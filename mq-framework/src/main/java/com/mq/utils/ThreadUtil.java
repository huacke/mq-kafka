package com.mq.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @ClassName ThreadUtil
 * @Description 线程工具类
 * @Version 1.0
 **/
public class ThreadUtil {
    //进程ID
    public static Integer processId=0;
    static {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String name = runtime.getName();
            processId = Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Throwable e) {
        }
    }
    public static  int coreNum  = Runtime.getRuntime().availableProcessors();

    //偏io类型合适的线程数
    public static int ioPriorityNum = 2 * coreNum + 1;
    //偏cpu类型合适的线程数
    public static int cpuPriorityNum =  coreNum + 1;

    public static int getIoPriorityThreadNum(Integer taskNum){
        if(null ==taskNum){
            return coreNum+1;
        }else if(taskNum <ioPriorityNum || taskNum.intValue() <=0){
            return taskNum;
        }
        return ioPriorityNum;
    }
    public static int getCpuPriorityThreadNum(Integer taskNum){
        if(null ==taskNum){
            return cpuPriorityNum;
        }else if(taskNum <cpuPriorityNum || taskNum.intValue() <=0){
            return taskNum;
        }
        return  cpuPriorityNum;
    }

}
