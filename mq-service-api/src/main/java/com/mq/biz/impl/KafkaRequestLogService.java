package com.mq.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.biz.bean.KafkaRequestLog;
import com.mq.mongo.dao.KafkaRequestLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @version v1.0
 * @ClassName KafkaRequestLogService
 * @Description kafka消息请求日志服务
 */
@Service
@Primary
public class KafkaRequestLogService extends BaseMongoService<KafkaRequestLog,String>{
    @Autowired
    private KafkaRequestLogDao kafkaRequestLogDao;
    @Override
    protected BaseMongoDao populateDao() {
        return kafkaRequestLogDao;
    }
    /**
     * @Description 保存日志
     * @param  requestLog
     **/
    public boolean saveLog(KafkaRequestLog requestLog){
       return  save(requestLog)!=null;
    }

}
