package com.mq.biz.bean;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * kafka死亡消息表
 * 当消息多次补偿失败或超过一定时间消息没被处理
 * 消息进入死信队列
 */
@Data
@Document(collection = "kafka_deaded_msg_log")
public class KafkaDeadedMsgLog extends KafkaMsgLog {

}
