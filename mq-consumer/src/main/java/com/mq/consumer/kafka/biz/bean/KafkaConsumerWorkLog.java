package com.mq.consumer.kafka.biz.bean;

import com.mq.entity.BaseObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

/**
 * kafka消费工作日志表
 *
 */
@Data
@Document(collection = "kafka_consumer_work_log")
public class KafkaConsumerWorkLog extends BaseObject {
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
     * 编码
     */
    private String code;
    /**
     * 返回结果
     */
    private String result;

    /**
     * 有效状态 0,无效，1 有效
     */
    private String state;
    /**
     * 消息JSON
     */
    private String message;
    /**
     * 请求时间
     */
    private Date requestTime;
    /**
     * 响应时间
     */
    private Date responseTime;
    /**
     * 花费时间
     */
    private Integer costTime;
    /**
     * 创建时间
     */
    private Date createtime;

}
