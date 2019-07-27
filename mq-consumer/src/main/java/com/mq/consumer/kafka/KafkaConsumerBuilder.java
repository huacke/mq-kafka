package com.mq.consumer.kafka;

import com.mq.common.config.kafka.KafkaConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author huacke
 * @version v1.0
 * @ClassName KafkaConsumerBuilder
 * 默认采用最严格的配置，为了尽量保证消息幂等，牺牲了kafka消息的一部分吞吐量，如果对消息的幂等性要求不高，可以自己重载这里面的配置
 * @Description 消费者构建器
 */
@Component
@DependsOn("kafkaConfiguration")
public class KafkaConsumerBuilder {

    public  KafkaConsumer build(final Map<String,Object> confMap){
        Properties props = new Properties();
        //每次最多能从kafka拉取的消息数
	    props.put("max.poll.records", 500);
        //每次从kafka分区poll获取批量消息的处理超时时间（两次poll的间隔时间）
        //特别要注意业务执行逻辑尽量要快，超时了会引起分区再均衡，而且无法提交offset
        props.put("max.poll.interval.ms", 1800*1000);
        //每次最小从kafka拉取的消息字节数
        props.put("fetch.min.bytes", 1*1024);
        //每次最大从kafka拉取的消息字节数
        props.put("fetch.max.bytes", 450*1024);
        //每次从kafka拉取消息的间隔时间
	    props.put("fetch.max.wait.ms", 100);
        //每次最多能从kafka单个分区拉取消息的字节数
        props.put("max.partition.fetch.bytes", 150*1024);
        //从分区消费起始偏移  earliest：分区最起始位置 latest:分区消息最后偏移量位置
        props.put("auto.offset.reset", "earliest");
        //是否自动提交偏移量
        props.put("enable.auto.commit", "false");
        //消费者心跳超时时间，超过此时间，服务器没有收到消费者心跳，就会认为此消费者下线，从而触发发分区再均衡操作
        props.put("session.timeout.ms", 16*1000);
        //消费者发送心跳频率
        props.put("heartbeat.interval.ms", 2*1000);

        //粘性分配，是为了保证再分配的结果尽可能多的保有现有分配。
        props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.StickyAssignor");

        //消息键序列化类
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //消息值序列化类
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaProperties kafkaProperties = KafkaConfiguration.kafkaProperties;
        if(kafkaProperties!=null){
            Map<String, Object> kafkaPropertiesMaps = kafkaProperties.buildConsumerProperties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesMaps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaPropertiesMaps.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
            props.put(SaslConfigs.SASL_MECHANISM, kafkaPropertiesMaps.get(SaslConfigs.SASL_MECHANISM));
            String username=(String)kafkaPropertiesMaps.get("sasl.username");
            String password=(String)kafkaPropertiesMaps.get("sasl.password");
            String saslJaasConfPath=(String)kafkaPropertiesMaps.get("sasl.jaas.config");
            //如果配置了密码文件目录则走配置文件方式
            if(StringUtils.isNotBlank(saslJaasConfPath)){
                System.setProperty("java.security.auth.login.config",saslJaasConfPath);
            }else{
                props.put("sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username=\""+username+"\" password=\""+password+"\";");
            }
        }
        Optional.ofNullable(confMap).ifPresent(it->{
            props.putAll(confMap);
        });
        KafkaConsumer consumer = new KafkaConsumer<>(props);
        return consumer;

    }


}
