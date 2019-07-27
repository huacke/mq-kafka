package com.mq.consumer.kafka.dao;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerMsgLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @version v1.0
 * @ClassName KafkaMQConsumerMessageLogDao
 * @Description  kafkaMq 消费者消息日志
 */
@Repository
@Primary
public class KafkaConsumerMsgLogDao extends BaseMongoDao<KafkaConsumerMsgLog,String> {

}
