package com.mq.consumer.kafka.hook;


import com.mq.boot.hook.BootPreStopedHook;
import com.mq.consumer.kafka.mgr.KafkaWorkerManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 关闭消费者钩子
 * 关闭容器中所有的消费者
 */
@Slf4j
public class StopKafkaConsumerWorkerHook extends BootPreStopedHook{
    @Override
    public void run() {
        KafkaWorkerManager.stop();
    }

    @Override
    public int getOrder() {
        return 90;
    }
}
