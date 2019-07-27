package com.mq.kafka.biz.impl;

import com.mq.kafka.biz.api.KafkaMQService;
import com.mq.msg.MessageHeader;
import com.mq.kafka.producer.KafkaProducerManager;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.common.response.ResultHandleT;
import com.mq.common.utils.StringUtils;
import com.mq.common.exception.code.ErrorCode;
import com.mq.utils.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
@Primary
@Slf4j
public class KafkaMQServiceImpl implements KafkaMQService {

	@Resource
	private KafkaProducerManager producerManager;

	@Override
	public ResultHandleT sendMQSync(KafkaMessage message) {
		return sendMQSync(message, null);
	}

	@Override
	public ResultHandleT sendMQSync(KafkaMessage message, Long timeout) {
		ResultHandleT resultHandleT = checkParam(message);
		if (resultHandleT != null) {
			return resultHandleT;
		}
		try {
			producerManager.sendMQSync(message.getTopic(), message, timeout);
		} catch (Exception e) {
			log.error("MQ发送消息出错 : ",e);
			return ResultHandleT.error(ErrorCode.MQ_SEND_ERROR,e);
		}
		return ResultHandleT.ok();
	}

	@Override
	public ResultHandleT sendMQAsync(KafkaMessage message) {
		ResultHandleT resultHandleT = checkParam(message);
		if (resultHandleT != null) {
			return resultHandleT;
		}
		try {
			producerManager.sendMQAsync(message.getTopic(), message);
		} catch (Exception e) {
			log.error("MQ发送消息出错! : ",e);
			return ResultHandleT.error(ErrorCode.MQ_SEND_ERROR,e);
		}
		return ResultHandleT.ok(resultHandleT);
	}

	@Override
	public ResultHandleT compensateSendMQ(KafkaMessage message) {
		ResultHandleT resultHandleT = checkParam(message);
		if (resultHandleT != null) {
			return resultHandleT;
		}
		try {
			producerManager.sendMQAsync(message.getTopic(), message);
		} catch (Exception e) {
			log.error("MQ补偿发送消息时出错! : ",e);
			return ResultHandleT.error(ErrorCode.MQ_SEND_ERROR,e);
		}
		return ResultHandleT.ok(resultHandleT);
	}


	private ResultHandleT checkParam(KafkaMessage message) {
		boolean vaild = true;
		if (null == message || StringUtils.isBlank(message.getMsgType()) || null == message.getHeader()) {
			vaild = false;
		} else {
			MessageHeader header = message.getHeader();
			String msgid = header.getMsgId();
			String srcSystem = header.getSourceSystem();
			String topic = message.getTopic();
			if (StringUtils.isBlank(msgid) || StringUtils.isBlank(srcSystem) || StringUtils.isBlank(topic)) {
				vaild = false;
			}
		}
		if (!vaild) {
			log.error("MQ消息参数错误! param : ", GsonHelper.toJson(message));
			return ResultHandleT.error(ErrorCode.MQ_PARAM_ERROR);
		}
		return null;
	}

}
