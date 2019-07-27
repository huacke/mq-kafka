package com.mq.redis;

/**
 * @ClassName RedisKey
 * @Description 缓存定义key
 * @Version 1.0
 **/
public class RedisKey {

    //缓存key前缀,项目名称
    private static final String PRE_FIX_NAME_MQ_SERVICE = "MQ_SERVICE_";
    //缓存key
    private static final String KAFKA_MQ_MESSAGE_LOG_TASK_LOCK_KEY = "KAFKA_MQ_MESSAGE_LOG_TASK_LOCK_KEY";

    //获取kafka消息服务日志定时任务锁key
    public static String getKAFKA_MQ_MESSAGE_LOG_LOCK_KEY(Object... args) {
        return String.format(PRE_FIX_NAME_MQ_SERVICE+KAFKA_MQ_MESSAGE_LOG_TASK_LOCK_KEY, args);
    }

}
