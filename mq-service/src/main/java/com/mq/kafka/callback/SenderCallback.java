package com.mq.kafka.callback;

import com.mq.biz.bean.KafkaMsgLog;
import com.mq.biz.impl.KafkaMsgLogService;
import com.mq.biz.impl.KafkaProcessMsgLogService;
import com.mq.kafka.scheduler.KakaMQMsgHandleTaskExecutor;
import com.mq.msg.enums.MsgStatus;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.thread.task.TaskFactory;
import com.mq.utils.SpringUtils;
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

	private static KafkaMsgLogService   kafkaMsgLogService = SpringUtils.getBean(KafkaMsgLogService.class);
	private static KafkaProcessMsgLogService kafkaMQProcessMessageLogService = SpringUtils.getBean(KafkaProcessMsgLogService.class);
	private static KakaMQMsgHandleTaskExecutor kakaMQMsgHandleTaskExecutor = SpringUtils.getBean(KakaMQMsgHandleTaskExecutor.class);

	KafkaMessage message;

	public SenderCallback(KafkaMessage message){
		this.message = message;
	}
	@Override
	public void onCompletion(RecordMetadata metadata, Exception e) {
		if (e != null) {
			log.error("===========发送消息到kafka队列出错，原因 ：", e);
		} else {
			log.info(String.format("发送消息到kafka队列成功！===》 topic:%s  parttion: %d offset : %d  ", metadata.topic(), metadata.partition(), metadata.offset()));
		}
		handle4MessageLog(message,metadata,e);
	}

	/**
	 * 处理消息日志
	 * @param message
	 * @param metadata
	 * @param err
	 */
	private void handle4MessageLog(KafkaMessage message,RecordMetadata metadata,Exception err){

		kakaMQMsgHandleTaskExecutor.submitRunTask(TaskFactory.createRunTask(new Runnable() {
			@Override
			public void run() {
				try{
					String topic = metadata.topic();
					Integer partition=  metadata.partition();
					Long  offset = metadata.offset();
					String id = message.getDigest();
					String status=null;
					if(topic.equals(message.getTopic())){
						if(err==null){
							status=MsgStatus.NORMAL.getCode();
							boolean rs=false;
							try{
								rs=kafkaMQProcessMessageLogService.deleteById(id);
							}catch (Exception e){
							}
							if(!rs){
								Update update = new Update();
								update.set("status",status);
								update.set("partition",partition);
								update.set("offset",offset);
								kafkaMQProcessMessageLogService.updateById(id, update);
							}
							KafkaMsgLog kafkaMsgLog = kafkaMsgLogService.makeLogFromMessage(message);
							kafkaMsgLog.setStatus(status);
							kafkaMsgLog.setOffset(offset);
							kafkaMsgLog.setPartition(partition);
							kafkaMsgLogService.saveLog(kafkaMsgLog);

						}else{
							status=MsgStatus.FAIL.getCode();
							Update update = new Update();
							update.set("status",status);
							update.set("partition",partition);
							kafkaMQProcessMessageLogService.updateById(id, update);
						}
					}
				}catch (Exception e){
					log.error("handle4MessageLog error cause by:",e);
				}
			}
		}));
	}

}
