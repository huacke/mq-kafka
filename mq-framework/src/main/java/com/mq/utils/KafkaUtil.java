package com.mq.utils;

import com.mq.msg.MessageHeader;
import com.mq.msg.enums.*;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.msg.kafka.TopicDef;
/**
 * Kafka工具类
 */
public class KafkaUtil {
    /**
     * 检查消息是否有效
     * @param kafkaMessage
     * @return
     */
    public static boolean checkKafkaMessage(KafkaMessage kafkaMessage) {
        if (   null == kafkaMessage
            || null == kafkaMessage.getHeader()
            || null == kafkaMessage.getMessageBody())
               return false;

        String topic = kafkaMessage.getTopic();
        String msgType = kafkaMessage.getMsgType();
        String status = kafkaMessage.getStatus();
        MsgStatus mst = MsgStatus.getTypeByCode(status);
        TopicDef.Topic tp = TopicDef.Topic.getTopicByCode(topic);
        MsgType mt = UsualMsgType.MSG_SEND.getTypeByCode(msgType);
        MessageHeader header = kafkaMessage.getHeader();
        String msgId = header.getMsgId();
        String digest = kafkaMessage.getDigest();
        if (null == tp
                || null==mt
                || null==mst
                || StringUtil.isEmpty(msgId)
                || StringUtil.isEmpty(digest)
                ) {
            return false;
        }

        return true;
    }


}
