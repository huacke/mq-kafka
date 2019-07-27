package com.mq.consumer.kafka.biz.bean;

import com.mq.entity.BaseObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * kafka消费日志表
 *
 */
@Data
@Document(collection = "kafka_consumer_msg_log")
public class KafkaConsumerMsgLog extends BaseObject {
    /**
     * 主键,digest_topic_partition_groupId
     */
    @Id
    private String id;
    /**
     * 消息摘要
     */
    private String digest;
    /**
     * 消息Id
     */
    private String msgId;
    /**
     * 来源消息Id
     */
    private String sourceMsgId;
    /**
     * 消息主题
     */
    private String topic;
    /**
     * 消息分区
     */
    private Integer partition;
    /**
     * 消费组
     */
    private String  groupId;
    /**
     * 消息偏移量
     */
    private Long  offset;
    /**
     * 消息来源系统
     */
    private String sourceSystem;
    /**
     * 消息类型
     */
    private String msgType;
    /**
     * 消息子类型
     */
    private String msgSubType;
    /**
     * 消息业务主键id,eg: orderId,goodId
     */
    private Object msgKey;
    /**
     * 消息状态
     */
    private String status;

    /**
     * 有效状态 0,无效，1 有效
     */
    private String state;
    /**
     * 消息JSON
     */
    private String message;
    /**
     * 创建时间
     */
    private Date createtime;

}
