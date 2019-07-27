package com.mq.mongo.dao;

import com.mq.biz.bean.KafkaRequestLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
/**
 * @version v1.0
 * @ClassName KafkaRequestLogDao
 * @Description  kafkaMq 消息请求日志
 */
@Repository
@Primary
public class KafkaRequestLogDao extends BaseMongoDao<KafkaRequestLog,String> {

}
