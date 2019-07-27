package com.mq.mongo.dao;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.biz.bean.KafkaDeadedMsgLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @version v1.0
 * @ClassName KafkaDeadedMsgLogDao
 * @Description  kafkaMq 死亡消息日志
 */
@Repository
@Primary
public class KafkaDeadedMsgLogDao extends BaseMongoDao<KafkaDeadedMsgLog,String> {

}
