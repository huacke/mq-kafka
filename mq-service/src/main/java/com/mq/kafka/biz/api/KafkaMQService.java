package com.mq.kafka.biz.api;

import com.mq.msg.kafka.KafkaMessage;
import com.mq.common.response.ResultHandleT;
/**
 * @author huacke
 * @version v1.0
 * @ClassName MQService
 * @Description kafka消息队列服务
 */
public interface KafkaMQService {
	/**
	 * @Description  同步发送消息
	 * @param message 消息对象
	 * @return ResultHandleT
	 **/
	public ResultHandleT sendMQSync(KafkaMessage message);
	/**
	 * @Description  同步发送消息
	 * @param message 消息对象
	 * @param timeout 超时时间：单位ms
	 * @return ResultHandleT
	 **/
	public ResultHandleT sendMQSync(KafkaMessage message,Long timeout);
	/**
	 * @Description  同步发送消息
	 * @param message 消息对象
	 * @return ResultHandleT
	 **/
	public ResultHandleT sendMQAsync(KafkaMessage message);
	/**
	 * @Description  补偿消息
	 * @param message 消息对象
	 * @return ResultHandleT
	 **/
	public ResultHandleT compensateSendMQ(KafkaMessage message);

}
