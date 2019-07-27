package com.mq.consumer.kafka.dao;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerOffsetLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @version v1.0
 * @ClassName KafkaConsumerOffsetLogDao
 * @Description  kafkaMq 消费者消费偏移量
 */
@Repository
@Primary
public class KafkaConsumerOffsetLogDao extends BaseMongoDao<KafkaConsumerOffsetLog,String> {

}
