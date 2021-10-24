package com.mq.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.biz.bean.KafkaDeadedMsgLog;
import com.mq.mongo.dao.KafkaDeadedMsgLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
/**
 * @version v1.0
 * @ClassName KafkaDeadedMsgLogService
 * @Description kafka死亡消息日志类
 */
@Service
@Primary
public class KafkaDeadedMsgLogService extends BaseMongoService<KafkaDeadedMsgLog,KafkaDeadedMsgLogDao,String>{

    @Autowired
    private KafkaDeadedMsgLogDao kafkaDeadedMsgLogDao;

    /**
     * @Description 保存日志
     * @param  kafkaDeadedMsgLog
     **/
    public boolean saveLog(KafkaDeadedMsgLog kafkaDeadedMsgLog){
        return save(kafkaDeadedMsgLog)!=null;
    }

}
