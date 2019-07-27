package com.mq.consumer.kafka.handler;


import com.mq.msg.kafka.KafkaMessage;

/**
 * @author huacke
 * @version v1.0
 * @ClassName KafkaMessageHandler
 * @Description kafka消息处理器,内部抽象类 用于实现自己的处理逻辑
 */
public  abstract class KafkaMessageHandler<T,R> {
    //执行消息处理
    public abstract  R doHandle(KafkaMessage<T> message);
    //执行处理
    public   R handle(KafkaMessage<T>  message){
        return doHandle(message);
    };
}
