package com.mq.biz.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.impl.BaseMongoService;
import com.mq.biz.bean.KafkaProcessMsgLog;
import com.mq.mongo.dao.KafkaProcessMsgLogDao;
import com.mq.msg.enums.MsgStatus;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.utils.GsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @version v1.0
 * @ClassName KafkaProcessMsgLogService
 * @Description kafka正在处理的消息日志服务
 */
@Service
@Primary
public class KafkaProcessMsgLogService extends BaseMongoService<KafkaProcessMsgLog,String>{

    @Autowired
    private KafkaProcessMsgLogDao kafkaProcessMsgLogDao;

    @Override
    protected BaseMongoDao populateDao() {
        return kafkaProcessMsgLogDao;
    }
    /**
     * @Description 保存日志
     * @param  kafkaProcessMsgLog
     **/
    public boolean saveLog(KafkaProcessMsgLog kafkaProcessMsgLog){
        Integer version = kafkaProcessMsgLog.getVersion();
        kafkaProcessMsgLog.setVersion(++version);
        return save(kafkaProcessMsgLog)!=null;
    }
    /**
     * @Description 根据消息生成日志对象
     **/
    public  KafkaProcessMsgLog makeLogFromMessage(KafkaMessage message){
        if(null==message){
            return null;
        }
        KafkaProcessMsgLog kafkaProcessMsgLog = new KafkaProcessMsgLog();
        kafkaProcessMsgLog.setId(message.getDigest());
        kafkaProcessMsgLog.setTopic(message.getTopic());
        kafkaProcessMsgLog.setStatus(message.getStatus());
        kafkaProcessMsgLog.setSourceMsgId(message.getHeader().getSourceMsgId());
        kafkaProcessMsgLog.setMsgId(message.getHeader().getMsgId());
        kafkaProcessMsgLog.setSourceSystem(message.getHeader().getSourceSystem());
        kafkaProcessMsgLog.setDeaded(MsgStatus.DEAD.equals(message.getStatus())?1:0);
        kafkaProcessMsgLog.setSrcHost(message.getHeader().getSrcHost());
        kafkaProcessMsgLog.setMsgKey(message.getMessageBody().getMsgKey());
        kafkaProcessMsgLog.setMsgType(message.getMsgType());
        kafkaProcessMsgLog.setMsgSubType(message.getMsgSubType());
        kafkaProcessMsgLog.setState("1");
        kafkaProcessMsgLog.setRetrytimes(0);
        kafkaProcessMsgLog.setVersion(0);
        kafkaProcessMsgLog.setCreateId("0");
        kafkaProcessMsgLog.setUpdateId("0");
        kafkaProcessMsgLog.setRemark("");
        kafkaProcessMsgLog.setMessage(GsonHelper.toJson(message));
        kafkaProcessMsgLog.setCreatetime(message.getHeader().getCreatetime());
        kafkaProcessMsgLog.setUpdateTime(message.getHeader().getCreatetime());
        return  kafkaProcessMsgLog;
    }



}
