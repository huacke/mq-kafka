package com.mq.consumer.kafka.dao;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerWorkLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @version v1.0
 * @ClassName KafkaConsumerWorkLogDao
 * @Description  kafkaMq 消费者业务处理执行的日志记录
 */
@Repository
@Primary
public class KafkaConsumerWorkLogDao extends BaseMongoDao<KafkaConsumerWorkLog,String> {
}
