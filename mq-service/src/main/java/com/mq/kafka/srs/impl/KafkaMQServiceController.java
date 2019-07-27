package com.mq.kafka.srs.impl;

import com.mq.kafka.biz.api.KafkaMQService;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.common.response.ResultHandleT;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Api(tags = "MQ消息服务")
@RestController
@RequestMapping(value = "/mq/kafka")
public class KafkaMQServiceController {

	@Autowired
	private KafkaMQService mqService;

	@ApiOperation(value = "同步发送消息", notes = "")
	@PostMapping(value = "/sendMQSync", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResultHandleT sendMQSync(@RequestBody KafkaMessage message) {
		return mqService.sendMQSync(message);
	}

	@ApiOperation(value = "异步发送消息", notes = "")
	@PostMapping(value = "/sendMQAsync", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResultHandleT sendMQAsync(@RequestBody KafkaMessage message) {
		return mqService.sendMQAsync(message);
	}

	@ApiOperation(value = "同步发送消息", notes = "")
	@PostMapping(value = "sendMQSyncT",produces= MediaType.APPLICATION_JSON_UTF8_VALUE,consumes=MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResultHandleT sendMQSyncT(@RequestBody KafkaMessage message,@RequestParam(name="timeout") Long timeout){
		return  mqService.sendMQSync(message,timeout);
	}

}