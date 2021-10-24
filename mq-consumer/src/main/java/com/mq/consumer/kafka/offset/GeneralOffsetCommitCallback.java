package com.mq.consumer.kafka.offset;

import com.mq.consumer.kafka.KafkaConsumerManager;
import com.mq.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
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

    private String groupId;

    private Map<TopicPartition, OffsetAndMetadata> currentOffsets;

    private static KafkaConsumerManager kafkaConsumerManager=SpringUtils.getBean(KafkaConsumerManager.class);


    private boolean saveFlag=false;
    private boolean showLog=false;

    public GeneralOffsetCommitCallback(String groupId,Map<TopicPartition, OffsetAndMetadata> currentOffsets,boolean saveFlag,boolean showLog){
        this.groupId =groupId;
        this.currentOffsets=currentOffsets;
        this.saveFlag=saveFlag;
        this.showLog=showLog;
    }

    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
        if (null == e) {
            map.entrySet().forEach(it -> {
                TopicPartition partition = it.getKey();
                OffsetAndMetadata offset = it.getValue();
                if(saveFlag){
                    kafkaConsumerManager.saveKafkaConsumerOffsetLog(
                            partition.topic(),
                            partition.partition(),
                            groupId,
                            offset.offset());
                }else{
                    OffsetAndMetadata currentOffsetAndMetadata = currentOffsets.get(partition);
                    if(currentOffsetAndMetadata!=null){
                        if(offset.offset()==currentOffsetAndMetadata.offset()){
                            currentOffsets.remove(partition);
                        }
                    }
                }
                if(showLog){
                    log.info(String.format("消费者提交偏移量到kafka成功  ===》 topic:%s,partition:%s,offset:%s ", partition.topic(), partition.partition(), offset.offset()));
                }
            });
        } else {
            Optional.ofNullable(map).ifPresent(itt -> {
                itt.entrySet().forEach(it -> {
                    TopicPartition partition = it.getKey();
                    OffsetAndMetadata offset = it.getValue();
                    if(saveFlag){
                        kafkaConsumerManager.saveKafkaConsumerOffsetLog(
                                partition.topic(),
                                partition.partition(),
                                groupId,
                                offset.offset());
                    }
                    if(showLog){
                        log.error(String.format("消费者提交偏移量到kafka失败 ===》 topic:%s,partition:%s,offset:%s  cause by: ", partition.topic(), partition.partition(), offset.offset()), e);
                    }
                });
            });
        }



    }
}