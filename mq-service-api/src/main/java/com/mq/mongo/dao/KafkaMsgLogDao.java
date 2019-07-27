package com.mq.mongo.dao;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.biz.bean.KafkaMsgLog;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
/**
 * @version v1.0
 * @ClassName KafkaMsgLogDao
 * @Description  kafkaMq 消息日志
 */
@Repository
@Primary
public class KafkaMsgLogDao extends BaseMongoDao<KafkaMsgLog,String> {

}
