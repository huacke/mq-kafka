package com.mq.consumer.kafka.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.lang.reflect.Field;

/**
 * @version v1.0
 * @ClassName KafkaConsumerUtil
 * @Description 消费者工具类
 */
@Slf4j
public class KafkaConsumerUtil {
    /**
     * 获取消费者组Id
     * @param consumer
     * @return
     */
    public static String getGroupId(KafkaConsumer consumer) {
        String groupId = null;
        try {
            Field groupIdField = consumer.getClass().getDeclaredField("groupId");
            if (groupIdField != null) {
                groupIdField.setAccessible(true);
                groupId = (String) groupIdField.get(consumer);
            }
        } catch (Exception e) {
            log.error(" consumer getGroupIdField error cause by: ", e);
        }
        return groupId;
    }
}
