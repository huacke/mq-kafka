package com.mq.consumer.kafka.biz.bean;

import com.mq.entity.BaseObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * kafka消费者消费偏移量
 */
@Data
@Document(collection = "kafka_consumer_offset_log")
public class KafkaConsumerOffsetLog extends BaseObject {
    /**
     * 主键,topic_partition_groupId
     */
    @Id
    private String id;
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
     * 有效状态 0,无效，1 有效
     */
    private String state;
    /*
     * 修改时间
     */
    private Date updateTime;
    /**
     * 创建时间
     */
    private Date createtime;

}
