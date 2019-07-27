package com.mq.biz.client.api;

import com.mq.msg.kafka.KafkaMessage;
import com.mq.common.response.ResultHandleT;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "mq-service",path = "mq-service/mq/kafka/")
public interface KafkaMQClientService {

	@PostMapping(value = "sendMQSync",produces= MediaType.APPLICATION_JSON_UTF8_VALUE,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResultHandleT sendMQSync(@RequestBody KafkaMessage message) throws Exception;

	@PostMapping(value = "sendMQSyncT",produces= MediaType.APPLICATION_JSON_UTF8_VALUE,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResultHandleT sendMQSyncT(@RequestBody KafkaMessage message,@RequestParam(name="timeout") Long timeout) throws Exception;

	@PostMapping(value = "sendMQAsync",produces= MediaType.APPLICATION_JSON_UTF8_VALUE,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResultHandleT sendMQAsync(@RequestBody KafkaMessage message)throws Exception;

}
