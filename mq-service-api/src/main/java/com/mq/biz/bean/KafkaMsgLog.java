package com.mq.biz.bean;

import com.mq.common.entity.BaseObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

/**
 * kafka全量消息日志表
 * 当消息状态流转完毕（消息状态： 初始--》发送成功 或 初始==》死亡）时消息会进入此表
 */
@Data
@Document(collection = "kafka_msg_log")
public class KafkaMsgLog extends BaseObject {
    /**
     * 主键,对应digest信息
     */
    @Id
    private String id;
    /**
     * 基于消息对象生成摘要，用来在业务上区分消息的唯一性
     */
    private String businessDigest;
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
     * 消息偏移量
     */
    private Long  offset;
    /**
     * 消息源ip
     */
    private String srcHost;
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
     * 是否 0否，1是
     */
    private Integer deaded;
    /**
     * 消息状态
     */
    private String status;
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
