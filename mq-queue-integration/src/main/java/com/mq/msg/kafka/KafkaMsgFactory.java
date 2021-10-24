package com.mq.msg.kafka;


import com.mq.msg.enums.MsgSubType;
import com.mq.msg.enums.MsgType;
import com.mq.common.sys.SystemID;

/**
 * @version v1.0
 * @ClassName KafkaMsgFactory
 * @Description kafka消息工厂
 */
public class KafkaMsgFactory {

    /**
     * @Description 创建消息
     * @param msgType 消息类型 com.walton.shop.msg.enums.MsgType 在子类中定义
     * @param appID  系统ID
     * @param topic  消息主题
     * @param msgData 数据
     * @return KafkaMessage 消息
     **/
    public static KafkaMessage createMsg(MsgType msgType, SystemID appID, TopicDef.Topic topic, Object msgData) {
        return createMsg(msgType, appID,topic,null, msgData);
    }


    /**
     * @Description 创建消息
     * @param msgType 消息类型 com.walton.shop.msg.enums.MsgType 在子类中定义
     * @param appID  系统ID
     * @param topic  消息主题
     * @param msgKey 消息键（eg:订单id，商品id等等），不设置默认为消息ID
     * @param msgData 数据
     * @return KafkaMessage 消息
     **/
    public static KafkaMessage createMsg(MsgType msgType, SystemID appID, TopicDef.Topic topic, Object msgKey, Object msgData) {
        return createMsg(msgType,null, appID,topic,msgKey, msgData);
    }


    /**
     * @Description 创建消息
     * @param msgType 消息类型 com.walton.shop.msg.enums.MsgType 在子类中定义
     * @param msgSubType 消息子类型 com.walton.shop.msg.enums.MsgSubType 在子类中定义
     * @param appID  系统ID
     * @param topic  消息主题
     * @param msgKey 消息键（eg:订单id，商品id等等），不设置默认为消息ID
     * @param msgData 数据
     * @return KafkaMessage 消息
     **/
    public static KafkaMessage createMsg(MsgType msgType, MsgSubType msgSubType, SystemID appID, TopicDef.Topic topic, Object msgKey, Object msgData) {
        return new KafkaMessage(msgType, msgSubType,appID,topic,msgKey, msgData);
    }


}
