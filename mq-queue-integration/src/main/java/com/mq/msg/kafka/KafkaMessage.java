package com.mq.msg.kafka;

import com.mq.msg.Message;
import com.mq.msg.enums.MsgSubType;
import com.mq.msg.enums.MsgType;
import com.mq.common.sys.SystemID;
import com.mq.utils.EncryptUtil;
import com.mq.utils.GsonHelper;
import com.mq.utils.KafkaUtil;
import lombok.Data;

/**
 * @version v1.0
 * @ClassName KafkaMessage<T>
 * B:body的class类型
 * T:数据的class类型
 * @Description  kafka消息对象
 */
@Data
public class KafkaMessage<T> extends Message<T> {


    private KafkaMessage(){}


    //消息主题
    private String topic;

    /**
     * @Description 创建消息
     * @param msgType 消息类型
     * @param systemID  系统ID
     * @param topic  消息主题
     * @param msgKey 消息键（eg:订单id，商品id等等），没有设置默认为消息ID
     * @param msgData 数据
     **/
    public KafkaMessage(MsgType msgType, SystemID systemID, TopicDef.Topic topic, Object msgKey, T msgData) {
        super(msgType,systemID,msgKey,msgData);
        createMessage(msgType,null,systemID,topic,msgKey,msgData);
    }

    /**
     * @Description 创建消息
     * @param msgType 消息类型
     * @param msgType 消息类型
     * @param msgSubType 消息子类型
     * @param topic  消息主题
     * @param msgKey 消息键（eg:订单id，商品id等等），没有设置默认为消息ID
     * @param msgData 数据
     **/
    public KafkaMessage(MsgType msgType, MsgSubType msgSubType, SystemID systemID, TopicDef.Topic topic, Object msgKey, T msgData) {
        super(msgType,systemID,msgKey,msgData);
        createMessage(msgType,msgSubType,systemID,topic,msgKey,msgData);
    }

    /**
     * @Description 创建消息
     * @param msgType 消息类型
     * @param msgType 消息类型
     * @param msgSubType 消息子类型
     * @param topic  消息主题
     * @param msgKey 消息键（eg:订单id，商品id等等），没有设置默认为消息ID
     * @param msgData 数据
     **/
    private void createMessage(MsgType msgType, MsgSubType msgSubType, SystemID systemID, TopicDef.Topic topic, Object msgKey, T msgData) {
        checkMsg(msgType,systemID,topic);
        initMsg(topic);
        if (!KafkaUtil.checkKafkaMessage(this)) {
            throw new IllegalArgumentException("消息格式错误！");
        }
    }
    /**
     * @Description 检查消息参数
     * @param msgType 消息类型
     * @param systemID  系统ID
     * @param topic  消息主题
     **/
    private void checkMsg(MsgType msgType, SystemID systemID, TopicDef.Topic topic){
        if(null ==msgType || null ==systemID || null==topic){
            throw new IllegalArgumentException("消息参数错误！");
        }
    }
    /**
     * @Description 初始化消息
     * @param  topic 主题
     * @return
     **/
    private void initMsg(TopicDef.Topic topic) {
        setTopic(topic.name());
        setDigest(digest());
    }
    /**
     * @Description 生成消息摘要
     **/
    protected String digest() {
        KafkaMsgDigest kafkaMsgDigest = new KafkaMsgDigest();
        kafkaMsgDigest.setTopic(getTopic());
        kafkaMsgDigest.setMsgId(getHeader().getMsgId());
        kafkaMsgDigest.setMsgType(getMsgType());
        kafkaMsgDigest.setMsgSubType(getMsgSubType());
        kafkaMsgDigest.setMessageBody(getMessageBody());
        kafkaMsgDigest.setCreatetime(getHeader().getCreatetime());
        String digest = null;
        try {
            digest = EncryptUtil.sha256(GsonHelper.toJson(kafkaMsgDigest));
        } catch (Exception e) {
        }
        return digest;
    }

}
