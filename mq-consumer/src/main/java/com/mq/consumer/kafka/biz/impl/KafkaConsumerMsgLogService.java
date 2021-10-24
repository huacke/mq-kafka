package com.mq.consumer.kafka.biz.impl;

import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerMsgLog;
import com.mq.consumer.kafka.dao.KafkaConsumerMsgLogDao;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.utils.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaConsumerMsgLogService
 * @Description kafka消费者消息日志
 */
@Slf4j
@Service
@Primary
public class KafkaConsumerMsgLogService extends BaseMongoService<KafkaConsumerMsgLog,KafkaConsumerMsgLogDao,String>{
    @Autowired
    private KafkaConsumerMsgLogDao kafkaConsumerMsgLogDao;
    /**
     * @Description 保存日志
     * @param  kafkaConsumerMsgLog
     **/
    public boolean saveLog(KafkaConsumerMsgLog kafkaConsumerMsgLog){
       return save(kafkaConsumerMsgLog)!=null;
    }


    public KafkaConsumerMsgLog queryKafkaConsumerMsgLog(String digest, String topic,String groupId){
        String id = generateConsumerLogId(digest,topic, groupId);
        return findById(id);
    }


    public boolean exists(String digest, String topic,String groupId){
        try {
            Query query = new Query();
            String id = generateConsumerLogId(digest,topic, groupId);
            query.addCriteria(Criteria.where("_id").is(id));
            return exists(query);
        } catch (Exception e) {
            log.error(" validateKafkaConsumerMsgLog error : ", e);
        }
        return false;
    }


    /**
     * @Description 生成消费者消息日志主键
     **/
    public String  generateConsumerLogId(String digest, String topic,String groupId){
        StringBuilder sb = new StringBuilder();
        sb.append(digest);
        sb.append("-");
        sb.append(topic);
        sb.append("-");
        sb.append(groupId);
        return sb.toString();
    }

    /**
     * @Description 构建kafka消费者日志
     **/
    public KafkaConsumerMsgLog buildKafkaConsumerMsgLog(KafkaMessage kafkaMessage, String topic, Integer partition, String groupId, Long offset){

        KafkaConsumerMsgLog kafkaConsumerMsgLog = new KafkaConsumerMsgLog();
        String id = generateConsumerLogId(kafkaMessage.getDigest(), topic, groupId);
        kafkaConsumerMsgLog.setId(id);
        kafkaConsumerMsgLog.setDigest(kafkaMessage.getDigest());
        kafkaConsumerMsgLog.setMsgId(kafkaMessage.getHeader().getMsgId());
        kafkaConsumerMsgLog.setMsgKey(kafkaMessage.getMessageBody().getMsgKey());
        kafkaConsumerMsgLog.setTopic(topic);
        kafkaConsumerMsgLog.setGroupId(groupId);
        kafkaConsumerMsgLog.setPartition(partition);
        kafkaConsumerMsgLog.setOffset(offset);
        kafkaConsumerMsgLog.setCreatetime(new Date());
        return kafkaConsumerMsgLog;
    }

    private byte[] kafkaConsumerMsgLogLock  = new byte[0];
    /**
     * @Description 保存消费日志
     **/
    public   boolean saveKafkaConsumerMsgLog(KafkaConsumerMsgLog kafkaConsumerMsgLog){
        try {
            synchronized (kafkaConsumerMsgLogLock){
                return saveLog(kafkaConsumerMsgLog);
            }
        }
        catch (Exception e){
            log.error("saveKafkaConsumerMsgLog error cause by: ", e);
        }
        return false;
    }


}
