package com.mq.msg.kafka;
import com.mq.msg.MessageBody;
import lombok.Data;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaMsgDigest
 * @Description kafka消息摘要对象，主要为了生成消息摘要，区分消息的唯一性
 */
@Data
public class KafkaMsgDigest {
    //消息ID
    private String msgId;
    //消息主题
    private String topic;
    //消息主类型
    private String msgType;
    //消息子类型
    private String msgSubType;
    //消息数据
    MessageBody messageBody;
    //创建时间
    private Date createtime;
}
