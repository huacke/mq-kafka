package com.mq.kafka.callback;

import com.mq.biz.bean.KafkaProcessMsgLog;
import com.mq.biz.impl.KafkaProcessMsgLogService;
import com.mq.common.utils.SpringUtils;
import com.mq.msg.enums.MsgStatus;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.data.mongodb.core.query.Update;

/**
 * @author huacke
 * @version v1.0
 * @ClassName SenderCallback
 * @Description 发送消息回调处理器
 */
@Slf4j
public class SenderCallback implements Callback {

	KafkaMessage message;

	public SenderCallback(KafkaMessage message){
		this.message = message;
	}
	@Override
	public void onCompletion(RecordMetadata metadata, Exception e) {
		if (e != null) {
			log.error("===========发送消息到kafka队列出错，原因 ：", e);
		} else {
			syncMessageStatus(message,metadata);
			log.info(String.format("发送消息到kafka队列成功！===》 topic:%s  parttion: %d offset : %d  ", metadata.topic(), metadata.partition(), metadata.offset()));
		}
	}
	private void syncMessageStatus(KafkaMessage message,RecordMetadata metadata){
		try{
			KafkaProcessMsgLogService kafkaMQProcessMessageLogService = SpringUtils.getBean(KafkaProcessMsgLogService.class);
			if(kafkaMQProcessMessageLogService!=null){
				String topic = metadata.topic();
				Integer partition=  metadata.partition();
				Long  offset = metadata.offset();
				String id = message.getDigest();
				if(topic.equals(message.getTopic())){
					if(StringUtil.isNotEmpty(id)){
						Update update = new Update();
						update.set("status",MsgStatus.NORMAL.getCode());
						update.set("partition",partition);
						update.set("offset",offset);
						boolean updated = kafkaMQProcessMessageLogService.updateById(id, update);
						if(!updated){
							KafkaProcessMsgLog kafkaProcessMsgLog = kafkaMQProcessMessageLogService.makeLogFromMessage(message);
							kafkaProcessMsgLog.setStatus(MsgStatus.NORMAL.getCode());
							kafkaProcessMsgLog.setPartition(partition);
							kafkaProcessMsgLog.setOffset(offset);
						}
					}
				}
			}
		}catch (Exception e){
			log.error("syncMessageStatus  error cause by :",e);
		}
	}

}
