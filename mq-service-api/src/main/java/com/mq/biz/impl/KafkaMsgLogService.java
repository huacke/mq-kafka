package com.mq.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.biz.bean.KafkaMsgLog;
import com.mq.mongo.dao.KafkaMsgLogDao;
import com.mq.msg.enums.MsgStatus;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.utils.GsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaMQMessageLogService
 * @Description kafka消息日志服务
 */
@Service
@Primary
public class KafkaMsgLogService extends BaseMongoService<KafkaMsgLog,KafkaMsgLogDao,String>{
    @Autowired
    private KafkaMsgLogDao kafkaMsgLogDao;
    /**
     * @Description 保存日志
     * @param  kafkaMsgLog
     **/
    public boolean saveLog(KafkaMsgLog kafkaMsgLog){
        return  save(kafkaMsgLog)!=null;
    }

    /**
     * @Description 根据消息生成日志对象
     **/
    public  KafkaMsgLog makeLogFromMessage(KafkaMessage message){
        if(null==message){
            return null;
        }
        KafkaMsgLog kafkaMsgLog = new KafkaMsgLog();
        kafkaMsgLog.setId(message.getDigest());
        kafkaMsgLog.setTopic(message.getTopic());
        kafkaMsgLog.setStatus(message.getStatus());
        kafkaMsgLog.setSourceMsgId(message.getHeader().getSourceMsgId());
        kafkaMsgLog.setMsgId(message.getHeader().getMsgId());
        kafkaMsgLog.setDeaded(MsgStatus.DEAD.equals(message.getStatus())?1:0);
        kafkaMsgLog.setMsgKey(message.getMessageBody().getMsgKey());
        kafkaMsgLog.setMsgType(message.getMsgType());
        kafkaMsgLog.setMsgSubType(message.getMsgSubType());
        kafkaMsgLog.setMessage(GsonHelper.toJson(message));
        kafkaMsgLog.setCreatetime(message.getHeader().getCreatetime());
        kafkaMsgLog.setUpdateTime(new Date());

        return  kafkaMsgLog;
    }

}
