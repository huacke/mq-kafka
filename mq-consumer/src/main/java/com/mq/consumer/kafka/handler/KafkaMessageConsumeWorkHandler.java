package com.mq.consumer.kafka.handler;


import com.mq.msg.kafka.KafkaMessage;

/**
 * @author huacke
 * @version v1.0
 * @ClassName KafkaMessageConsumeWorkHandler
 * @Description kafka消息消费工作处理器
 */
public  class KafkaMessageConsumeWorkHandler<T,R> extends KafkaMessageHandler<T,R>{
    @Override
    public R doHandle(KafkaMessage<T> message) {
        return null;
    }
}
