package com.mq.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.biz.bean.KafkaMsgLog;
import com.mq.mongo.dao.KafkaMsgLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @version v1.0
 * @ClassName KafkaMQMessageLogService
 * @Description kafka消息日志服务
 */
@Service
@Primary
public class KafkaMsgLogService extends BaseMongoService<KafkaMsgLog,String>{
    @Autowired
    private KafkaMsgLogDao kafkaMsgLogDao;
    @Override
    protected BaseMongoDao populateDao() {
        return kafkaMsgLogDao;
    }
    /**
     * @Description 保存日志
     * @param  kafkaMsgLog
     **/
    public boolean saveLog(KafkaMsgLog kafkaMsgLog){
        Integer version = kafkaMsgLog.getVersion();
        kafkaMsgLog.setVersion(++version);
        return  save(kafkaMsgLog)!=null;
    }

}
