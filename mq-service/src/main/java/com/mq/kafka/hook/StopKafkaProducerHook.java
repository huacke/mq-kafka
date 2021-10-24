package com.mq.kafka.hook;


import com.mq.boot.hook.BootPreStopedHook;
import com.mq.kafka.producer.KafkaProducerManager;
import com.mq.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 关闭Producer钩子
 */
@Slf4j
public class StopKafkaProducerHook extends BootPreStopedHook{

    @Override
    public void run() {
        KafkaProducerManager kafkaProducerManager  = SpringUtils.getBean(KafkaProducerManager.class);
        kafkaProducerManager.shutdown();
    }

    @Override
    public int getOrder() {
        return 210;
    }
}
