package com.mq.consumer.kafka.partition;

import com.mq.common.utils.SpringUtils;
import com.mq.consumer.kafka.KafkaConsumerManager;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerOffsetLog;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerOffsetLogService;
import com.mq.consumer.kafka.util.KafkaConsumerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huacke
 * @version v1.0
 * @ClassName HandleRebalance
 * @Description 分区再均衡处理器
 */
@Slf4j
public class HandleRebalance implements ConsumerRebalanceListener {

    private KafkaConsumerOffsetLogService kafkaConsumerOffsetLogService = SpringUtils.getBean(KafkaConsumerOffsetLogService.class);

    private KafkaConsumer consumer;

    private Thread thread;

    public HandleRebalance(KafkaConsumer consumer,Thread thread) {
        this.consumer = consumer;
        this.thread =thread;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        log.warn("消费者分区重新平衡，开始提交对应分区分组的消费者偏移量》》》》");
        commitOffset(partitions);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        String groupId  = KafkaConsumerUtil.getGroupId(consumer);;
        partitions.forEach((topicPartition) -> {
            String topic = topicPartition.topic();
            int partition = topicPartition.partition();
            OffsetAndMetadata committedOffset = consumer.committed(topicPartition);
            if(committedOffset!=null){
                try{
                    consumer.seek(topicPartition,committedOffset.offset());
                    log.info(String.format("主题：%s,分区：%s,消费组： %s,重新平衡成功，当前偏移量为：%s  ",topic,partition,groupId,committedOffset.offset()));
                }catch (Exception e){
                    log.error(String.format("主题：%s,分区：%s,消费组： %s,重新平衡成功，当前偏移量为：%s ，重置本地偏移量时出错,原因: ",topic,partition,groupId,committedOffset.offset()),e);
                }
            }
        });
        log.warn("消费者分区重新平衡结束》》》》");
    }



    private void commitOffset(Collection<TopicPartition> partitions) {
        String groupId  = KafkaConsumerUtil.getGroupId(consumer);;
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetMap = KafkaConsumerManager.currentOffsetHolder.get();
        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = new HashMap<>();
        partitions.forEach((topicPartition) -> {
            String topic = topicPartition.topic();
            int partition = topicPartition.partition();
            OffsetAndMetadata offsetAndMetadata = topicPartitionOffsetMap.get(topicPartition);
            if (offsetAndMetadata != null) {
                offsetAndMetadataMap.put(topicPartition, offsetAndMetadata);
                long offset = offsetAndMetadata.offset();
                try {
                    consumer.commitSync(offsetAndMetadataMap);
                    offsetAndMetadataMap.remove(topicPartition);
                    KafkaConsumerOffsetLog kafkaConsumerOffsetLog = kafkaConsumerOffsetLogService.buildKafkaConsumerOffsetLog(topic, partition, groupId, offset);
                    saveKafkaConsumerOffsetLog(kafkaConsumerOffsetLog);
                    log.info(String.format("消费分区再均衡，提交偏移量到kafka成功  ===》 topic:%s,partition:%s,offset:%s ", topic, partition, offset));
                } catch (Exception e) {
                    log.error(String.format("消费分区再均衡，提交偏移量到kafka出错 ===》 topic:%s,partition:%s,offset:%s ，原因:  ", topic, partition, offset), e);
                }
                topicPartitionOffsetMap.remove(topicPartition);
            }
        });
        topicPartitionOffsetMap.clear();

    }
    /**
     * @Description 保存消费偏移量日志
     **/
    private boolean saveKafkaConsumerOffsetLog(KafkaConsumerOffsetLog kafkaConsumerOffsetLog) {
        try {
           return  kafkaConsumerOffsetLogService.saveLog(kafkaConsumerOffsetLog);
        } catch (Exception e) {
            log.error("消费分区再均衡，保存消费偏移量日志出错 : ", e);
        }
        return false;
    }

}
