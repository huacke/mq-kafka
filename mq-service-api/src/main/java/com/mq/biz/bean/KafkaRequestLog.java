package com.mq.biz.bean;

import com.mq.common.entity.BaseObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

/**
 * kafka消息服务请求日志表
 * 记录每次服务接口调用的日志
 */
@Data
@Document(collection = "kafka_request_log")
public class KafkaRequestLog extends BaseObject {
    /**
     * 主键，日志ID
     */
    @Id
    private String id;
    /**
     * 方法名
     */
    private String api;
    /**
     * 节点（机器Ip）
     */
    private String node;
    /**
     * 摘要
     **/
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
     * 消息来源系统
     */
    private String sourceSystem;
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
     * 消息状态
     */
    private String status;

    /**
     * 参数
     */
    private String param;
    /**
     * 编码
     */
    private String code;
    /**
     * 返回结果
     */
    private String result;

    /**
     * 请求时间
     */
    private Date requestTime;
    /**
     * 花费时间
     */
    private Integer costTime;
    /**
     * 创建时间
     */
    private Date createtime;

}
