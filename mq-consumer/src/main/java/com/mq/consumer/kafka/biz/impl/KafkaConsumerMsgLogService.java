package com.mq.consumer.kafka.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerMsgLog;
import com.mq.consumer.kafka.dao.KafkaConsumerMsgLogDao;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.utils.GsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaConsumerMsgLogService
 * @Description kafka消费者消息日志
 */
@Service
@Primary
public class KafkaConsumerMsgLogService extends BaseMongoService<KafkaConsumerMsgLog,String>{
    @Autowired
    private KafkaConsumerMsgLogDao kafkaConsumerMsgLogDao;
    @Override
    protected BaseMongoDao populateDao() {
        return kafkaConsumerMsgLogDao;
    }
    /**
     * @Description 保存日志
     * @param  kafkaConsumerMsgLog
     **/
    public boolean saveLog(KafkaConsumerMsgLog kafkaConsumerMsgLog){
       return save(kafkaConsumerMsgLog)!=null;
    }


    public KafkaConsumerMsgLog queryKafkaConsumerMsgLog(String digest, String topic, Integer partition, String groupId){
        String id = generateConsumerLogId(digest,topic, partition, groupId);
        return findById(id);
    }

    /**
     * @Description 生成消费者消息日志主键
     **/
    public String  generateConsumerLogId(String digest, String topic,Integer partition,String groupId){
        StringBuilder sb = new StringBuilder();
        sb.append(digest);
        sb.append("-");
        sb.append(topic);
        sb.append("-");
        sb.append(partition);
        sb.append("-");
        sb.append(groupId);
        return sb.toString();
    }

    /**
     * @Description 构建kafka消费者日志
     **/
    public KafkaConsumerMsgLog buildKafkaConsumerMsgLog(KafkaMessage kafkaMessage, String topic, Integer partition, String groupId, Long offset){

        KafkaConsumerMsgLog kafkaConsumerMsgLog = new KafkaConsumerMsgLog();
        String id = generateConsumerLogId(kafkaMessage.getDigest(), topic, partition, groupId);
        kafkaConsumerMsgLog.setId(id);
        kafkaConsumerMsgLog.setDigest(kafkaMessage.getDigest());
        kafkaConsumerMsgLog.setMsgId(kafkaMessage.getHeader().getMsgId());
        kafkaConsumerMsgLog.setSourceMsgId(kafkaMessage.getHeader().getSourceMsgId());
        kafkaConsumerMsgLog.setMessage(GsonHelper.toJson(kafkaMessage));
        kafkaConsumerMsgLog.setMsgKey(kafkaMessage.getMessageBody().getMsgKey());
        kafkaConsumerMsgLog.setMsgType(kafkaMessage.getMsgType());
        kafkaConsumerMsgLog.setMsgSubType(kafkaMessage.getMsgSubType());
        kafkaConsumerMsgLog.setTopic(topic);
        kafkaConsumerMsgLog.setGroupId(groupId);
        kafkaConsumerMsgLog.setPartition(partition);
        kafkaConsumerMsgLog.setOffset(offset);
        kafkaConsumerMsgLog.setSourceSystem(kafkaMessage.getHeader().getSourceSystem());
        kafkaConsumerMsgLog.setState("1");
        kafkaConsumerMsgLog.setStatus(kafkaMessage.getStatus());
        kafkaConsumerMsgLog.setCreatetime(new Date());
        return kafkaConsumerMsgLog;
    }

}
