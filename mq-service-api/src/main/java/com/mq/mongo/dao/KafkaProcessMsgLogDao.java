package com.mq.mongo.dao;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.biz.bean.KafkaProcessMsgLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @version v1.0
 * @ClassName KafkaProcessMsgLogDao
 * @Description  kafkaMq 正在处理的消息日志
 */
@Repository
@Primary
public class KafkaProcessMsgLogDao extends BaseMongoDao<KafkaProcessMsgLog,String> {

}
