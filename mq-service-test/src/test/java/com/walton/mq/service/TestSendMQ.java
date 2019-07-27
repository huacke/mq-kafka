package com.walton.mq.service;

import com.mq.biz.client.api.KafkaMQClientService;
import com.mq.common.response.ResultHandleT;
import com.mq.msg.enums.SystemID;
import com.mq.msg.enums.UsualMsgType;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.msg.kafka.KafkaMsgFactory;
import com.mq.msg.kafka.TopicDef;
import com.mq.utils.GsonHelper;
import com.walton.mq.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TestSendMQ extends BaseTest {
    @Autowired
    private KafkaMQClientService kafkaMQClientService;
    @Test
    public void testEureka() throws Exception {
        KafkaMessage msg = KafkaMsgFactory.createMsg(UsualMsgType.PAYED, SystemID.SERVICE, TopicDef.Topic.PAY, "ORD20190715140000001", "ORD20190715140000001");
        ResultHandleT resultHandleT = kafkaMQClientService.sendMQAsync(msg);
        System.out.println(GsonHelper.toJson(resultHandleT));
    }
}

