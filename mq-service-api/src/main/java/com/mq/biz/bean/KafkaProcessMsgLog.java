package com.mq.biz.bean;

import com.mq.common.entity.BaseObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

/**
 * kafka增量消息日志表
 * 保存的是当前正在流转状态的消息
 * 提供给定时任务进行消息补偿
 * 待消息发送到Kafka队列成功，或者多次补偿失败，成为死信，就会转移到全量消息表
 */
@Data
@Document(collection = "kafka_process_msg_log")
public class KafkaProcessMsgLog extends BaseObject {
    /**
     * 主键,对应digest信息
     */
    @Id
    private String id;
    /**
     * 消息Id
     */
    private String msgId;
    /**
     * 版本号
     */
    private Integer version;
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
     * 消息偏移量
     */
    private Long offset;
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
     * 重试次数
     */
    private Integer retrytimes;
    /**
     * 是否死亡 0否，1是
     */
    private Integer deaded;
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
    /*
     * 修改时间
     */
    private Date updateTime;
    /**
     * 创建时间
     */
    private Date createtime;

}
