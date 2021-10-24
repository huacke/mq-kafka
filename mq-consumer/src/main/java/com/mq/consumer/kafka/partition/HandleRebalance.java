package com.mq.consumer.kafka.partition;

import com.mq.consumer.kafka.biz.bean.KafkaConsumerOffsetLog;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerOffsetLogService;
import com.mq.consumer.kafka.util.KafkaConsumerUtil;
import com.mq.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.CollectionUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huacke
 * @version v1.0
 * @ClassName HandleRebalance
 * @Description 分区再均衡处理器
 */
@SuppressWarnings("ALL")
@Slf4j
public class HandleRebalance implements ConsumerRebalanceListener {

    private KafkaConsumerOffsetLogService kafkaConsumerOffsetLogService = SpringUtils.getBean(KafkaConsumerOffsetLogService.class);

    private KafkaConsumer consumer;

    private Map<TopicPartition, OffsetAndMetadata> currentOffsets;

    public HandleRebalance(KafkaConsumer consumer,Map<TopicPartition, OffsetAndMetadata> currentOffsets) {
        this.consumer = consumer;
        this.currentOffsets=currentOffsets;
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
            KafkaConsumerOffsetLog dbOffsetLog = queryDbOffset(topic, partition, groupId);
            Long offset=null;
            if(dbOffsetLog!=null){
                offset=dbOffsetLog.getOffset();
            }
            if(null==offset){
                OffsetAndMetadata committedOffset = consumer.committed(topicPartition);
                if(committedOffset!=null){
                    offset=committedOffset.offset();
                }
            }
            if(offset!=null){
                try{
                    consumer.seek(topicPartition,offset);
                    log.info(String.format("主题：%s,分区：%s,消费组： %s,重新平衡成功，当前偏移量为：%s  ",topic,partition,groupId,offset));
                }catch (Exception e){
                    log.error(String.format("主题：%s,分区：%s,消费组： %s,重新平衡成功，当前偏移量为：%s ，重置本地偏移量时出错,原因: ",topic,partition,groupId,offset),e);
                }
            }
        });
        log.warn("消费者分区重新平衡结束》》》》");
    }



    private void commitOffset(Collection<TopicPartition> partitions) {
        if (CollectionUtils.isEmpty(currentOffsets)) { return; }
        String groupId  = KafkaConsumerUtil.getGroupId(consumer);;
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetMap = currentOffsets;
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
                    topicPartitionOffsetMap.remove(topicPartition);
                    KafkaConsumerOffsetLog kafkaConsumerOffsetLog = kafkaConsumerOffsetLogService.buildKafkaConsumerOffsetLog(topic, partition, groupId, offset);
                    saveKafkaConsumerOffsetLog(kafkaConsumerOffsetLog);
                    log.info(String.format("消费分区再均衡，提交偏移量到kafka成功  ===》 topic:%s,partition:%s,offset:%s ", topic, partition, offset));
                } catch (Exception e) {
                    log.error(String.format("消费分区再均衡，提交偏移量到kafka出错 ===》 topic:%s,partition:%s,offset:%s ，原因:  ", topic, partition, offset), e);
                }
            }
        });
    }


    /**
     * @Description 查询消费偏移量日志
     **/
    private KafkaConsumerOffsetLog queryDbOffset(String topic, Integer partition, String groupId) {
        try {
            return  kafkaConsumerOffsetLogService.queryKafkaConsumerOffsetLog(topic,partition,groupId);
        } catch (Exception e) {
            log.error("消费分区再均衡，查询消费偏移量日志出错 : ", e);
        }
        return null;
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
