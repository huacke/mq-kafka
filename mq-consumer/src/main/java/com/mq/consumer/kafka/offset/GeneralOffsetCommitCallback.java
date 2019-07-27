package com.mq.consumer.kafka.offset;

import com.mq.common.utils.SpringUtils;
import com.mq.consumer.kafka.KafkaConsumerManager;
import com.mq.consumer.kafka.util.KafkaConsumerUtil;
import com.mq.msg.kafka.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import java.util.Map;
import java.util.Optional;
/**
 * @author huacke
 * @version v1.0
 * @ClassName GeneralOffsetCommitCallback
 * @Description 通用的消费者偏移量提交回调处理器
 */
@Slf4j
public class GeneralOffsetCommitCallback implements OffsetCommitCallback {

    KafkaConsumer consumer;

    private KafkaMessage kafkaMessage;

    private KafkaConsumerManager kafkaConsumerManager=SpringUtils.getBean(KafkaConsumerManager.class);

    private Map<TopicPartition, OffsetAndMetadata> currentOffsets;

    public GeneralOffsetCommitCallback(KafkaConsumer consumer, KafkaMessage kafkaMessage,Map<TopicPartition, OffsetAndMetadata> currentOffsets){
        this.consumer =consumer;
        this.kafkaMessage = kafkaMessage;
        this.currentOffsets=currentOffsets;
    }

    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
        if (null == e) {
            map.entrySet().forEach(it -> {
                TopicPartition partition = it.getKey();
                OffsetAndMetadata offset = it.getValue();
                if(currentOffsets.containsKey(partition)){
                    kafkaConsumerManager.saveLog(kafkaMessage,
                            partition.topic(),
                            partition.partition(),
                            KafkaConsumerUtil.getGroupId(consumer),
                            offset.offset());
                    currentOffsets.remove(partition);
                }
                log.info(String.format("消费者提交偏移量到kafka成功  ===》 topic:%s,partition:%s,offset:%s ", partition.topic(), partition.partition(), offset.offset()));
            });
        } else {
            Optional.ofNullable(map).ifPresent(itt -> {
                itt.entrySet().forEach(it -> {
                    TopicPartition partition = it.getKey();
                    OffsetAndMetadata offset = it.getValue();
                    log.error(String.format("消费者提交偏移量到kafka失败 ===》 topic:%s,partition:%s,offset:%s  cause by: ", partition.topic(), partition.partition(), offset.offset()), e);
                });
            });
        }
    }
}