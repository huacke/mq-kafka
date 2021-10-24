package com.mq.consumer.kafka.biz.impl;

import com.mq.common.exception.BussinessException;
import com.mq.common.response.ResultHandleT;
import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerWorkLog;
import com.mq.consumer.kafka.dao.KafkaConsumerMsgLogDao;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.utils.ExceptionFormatUtil;
import com.mq.utils.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaConsumerMsgLogService
 * @Description kafka消费者工作消息日志
 */
@Service
@Primary
@Slf4j
public class KafkaConsumerWorkLogService extends BaseMongoService<KafkaConsumerWorkLog,KafkaConsumerMsgLogDao,String>{
    @Autowired
    private KafkaConsumerMsgLogDao kafkaConsumerMsgLogDao;
    /**
     * @Description 保存日志
     * @param  kafkaConsumerWorkLog
     **/
    public boolean saveLog(KafkaConsumerWorkLog kafkaConsumerWorkLog){
       return save(kafkaConsumerWorkLog)!=null;
    }


    public KafkaConsumerWorkLog queryKafkaConsumerWorkLog(String digest, String topic, Integer partition, String groupId){
        String id = generateConsumerWorkLogId(digest,topic, partition, groupId);
        return findById(id);
    }
    /**
     * @Description 生成消费者消息工作日志主键
     **/
    public String  generateConsumerWorkLogId(String digest, String topic,Integer partition,String groupId){
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
     * @Description 构建kafka消费者工作日志
     **/
    public KafkaConsumerWorkLog buildKafkaConsumerWorkLog(KafkaMessage kafkaMessage, String topic, Integer partition, String groupId, Long offset){
        KafkaConsumerWorkLog kafkaConsumerWorkLog = new KafkaConsumerWorkLog();
        String id = generateConsumerWorkLogId(kafkaMessage.getDigest(), topic, partition, groupId);
        kafkaConsumerWorkLog.setId(id);
        kafkaConsumerWorkLog.setDigest(kafkaMessage.getDigest());
        kafkaConsumerWorkLog.setMsgId(kafkaMessage.getHeader().getMsgId());
        kafkaConsumerWorkLog.setMessage(GsonHelper.toJson(kafkaMessage));
        kafkaConsumerWorkLog.setMsgType(kafkaMessage.getMsgType());
        kafkaConsumerWorkLog.setMsgSubType(kafkaMessage.getMsgSubType());
        kafkaConsumerWorkLog.setTopic(topic);
        kafkaConsumerWorkLog.setGroupId(groupId);
        kafkaConsumerWorkLog.setPartition(partition);
        kafkaConsumerWorkLog.setOffset(offset);
        kafkaConsumerWorkLog.setCreatetime(new Date());
        return kafkaConsumerWorkLog;
    }


    public void saveKafkaConsumerWorkLog(ResultHandleT handleResult, KafkaMessage message, String topic, int partition, String groupId, long offset, Date begin, Date end) {

        try {
            KafkaConsumerWorkLog kafkaConsumerWorkLog = buildKafkaConsumerWorkLog(message, topic, partition, groupId, offset);
            kafkaConsumerWorkLog.setRequestTime(begin);
            if (begin != null && end != null) {
                long costTime = end.getTime() - begin.getTime();
                kafkaConsumerWorkLog.setCostTime((int) costTime);
            }
            if (handleResult != null) {
                Throwable error = handleResult.getE();
                if (error != null) {
                    if (error instanceof BussinessException) {
                        BussinessException err = (BussinessException) error;
                        kafkaConsumerWorkLog.setCode(err.getCode());
                    } else {
                        kafkaConsumerWorkLog.setCode("1");
                    }
                    kafkaConsumerWorkLog.setResult(ExceptionFormatUtil.getTrace(error, 5));
                } else {
                    kafkaConsumerWorkLog.setCode("0");
                    kafkaConsumerWorkLog.setResult(GsonHelper.toJson(handleResult.getData()));
                }
            }
            saveLog(kafkaConsumerWorkLog);
        } catch (Exception e) {
            log.error("saveKafkaConsumerWorkLog error cause by: ", e);
        }
    }

}
