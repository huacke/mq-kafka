package com.walton.mq.service;

import com.mq.biz.client.api.KafkaMQClientService;
import com.mq.common.response.ResultHandleT;
import com.mq.common.sys.SystemID;
import com.mq.msg.enums.UsualMsgType;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.msg.kafka.KafkaMsgFactory;
import com.mq.msg.kafka.TopicDef;
import com.walton.mq.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

@Slf4j
public class TestSendMQ extends BaseTest {
    @Autowired
    private KafkaMQClientService kafkaMQClientService;
    @Test
    public void testSendMQ() throws Exception {
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        for(int i=0;i<1000;i++){
            try{
                KafkaMessage msg = KafkaMsgFactory.createMsg(UsualMsgType.PAYED, SystemID.SERVICE, TopicDef.Topic.PAY, "ORD2019071514000000"+i, i);
                ResultHandleT resultHandleT = kafkaMQClientService.sendMQAsync(msg);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        stopWatch.stop();
        System.out.println("cost: "+stopWatch.getTotalTimeMillis());

    }
}

