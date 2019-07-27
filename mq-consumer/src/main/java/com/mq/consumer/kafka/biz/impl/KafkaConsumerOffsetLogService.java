package com.mq.consumer.kafka.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerOffsetLog;
import com.mq.consumer.kafka.dao.KafkaConsumerOffsetLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaConsumerOffsetLogService
 * @Description kafka消费者消费偏移量
 */
@Service
@Primary
public class KafkaConsumerOffsetLogService extends BaseMongoService<KafkaConsumerOffsetLog,String>{
    @Autowired
    private KafkaConsumerOffsetLogDao kafkaConsumerOffsetLogDao;
    @Override
    protected BaseMongoDao populateDao() {
        return kafkaConsumerOffsetLogDao;
    }
    /**
     * @Description 保存日志
     * @param  kafkaConsumerOffsetLog
     **/
    public boolean saveLog(KafkaConsumerOffsetLog kafkaConsumerOffsetLog){
       return save(kafkaConsumerOffsetLog)!=null;
    }

    public KafkaConsumerOffsetLog queryKafkaConsumerOffsetLog(String topic, Integer partition, String groupId){
        String id = generateConsumerOffsetLogId(topic, partition, groupId);
        return findById(id);
    }

    /**
     * @Description 生成消费者偏移量日志主键
     **/
    public String  generateConsumerOffsetLogId(String topic,Integer partition,String groupId){
        StringBuilder sb = new StringBuilder();
        sb.append(topic);
        sb.append("-");
        sb.append(partition);
        sb.append("-");
        sb.append(groupId);
        return sb.toString();
    }
    /**
     * @Description 构建kafka消费者偏移量日志
     **/
    public KafkaConsumerOffsetLog buildKafkaConsumerOffsetLog(String topic, Integer partition, String groupId, Long offset){
        KafkaConsumerOffsetLog kafkaConsumerOffsetLog = new KafkaConsumerOffsetLog();
        String id = generateConsumerOffsetLogId(topic, partition, groupId);
        kafkaConsumerOffsetLog.setId(id);
        kafkaConsumerOffsetLog.setTopic(topic);
        kafkaConsumerOffsetLog.setGroupId(groupId);
        kafkaConsumerOffsetLog.setPartition(partition);
        kafkaConsumerOffsetLog.setOffset(offset);
        kafkaConsumerOffsetLog.setState("1");
        kafkaConsumerOffsetLog.setCreatetime(new Date());
        kafkaConsumerOffsetLog.setUpdateTime(kafkaConsumerOffsetLog.getCreatetime());
        return kafkaConsumerOffsetLog;
    }
}
