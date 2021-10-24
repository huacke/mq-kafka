package com.mq.worker.order;

import com.mq.consumer.kafka.biz.bean.TopicGroup;
import com.mq.consumer.kafka.worker.DefaultKafkaMsgWorker;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.msg.kafka.TopicDef;
import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import com.mq.utils.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huacke
 * @version v1.0
 * @ClassName OrderPayMsgWorker
 */
@Slf4j
@Component
public class OrderPayMsgWorker extends DefaultKafkaMsgWorker<Void, String,KafkaMessage<String>> {

    @Override
    protected void configure() {

        List<TopicGroup> topicGroups = new ArrayList<TopicGroup>();
        topicGroups.add(new TopicGroup(TopicDef.Topic.PAY, TopicDef.Group.PAYED_PROCESS, 3));
        doConfigure(topicGroups,
                1000,
                600 * 1000l,
                15 * 1000l,
                50,
                null);
    }

    @Override
    public Void doWork(KafkaMessage<String> message) {
        try {
            log.info(GsonHelper.toJson(message));
        } catch (Exception e) {
            log.error("doWork error cause by : ", e);
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, e);
        }
        return null;
    }
}
