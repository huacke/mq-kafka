package com.mq.kafka.producer;

import com.mq.config.kafka.KafkaConfiguration;
import com.mq.kafka.partition.KeyHashPartitioner;
import com.mq.common.utils.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
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
 * @ClassName KafkaProducerBuilder
 * 默认采用最严格的配置，为了尽量保证消息幂等，牺牲了kafka消息的一部分吞吐量，如果对消息的幂等性要求不高，可以自己重载这里面的配置
 * @Description 生产者构建器
 */
@Component
@DependsOn("kafkaConfiguration")
public class KafkaProducerBuilder {

    public KafkaProducer build(final Map<String,Object> confMap){

        Properties props = new Properties();

        /**
         * 1. acks=0 意味着生产者能够通过网络吧消息发送出去，那么就认为消息已成功写入Kafka 一定会丢失一些数据
         * 2. acks=1 意味着首领在疏导消息并把它写到分区数据问津是会返回确认或者错误响应，还是可能会丢数据
         * 3. acks=all 意味着首领在返回确认或错误响应之前，会等待所有同步副本都收到消息。
         **/
        props.put("acks", "all");
        //消息发送失败再重试次数
        props.put("retries", Integer.MAX_VALUE);
        //批量发送消息数量
        props.put("batch.size", 1000);
        //批量发送消息时间间隔
        props.put("linger.ms", 100);
        //发送队列内存buffer容量
        props.put("buffer.memory", 128*1024*1024);
        //发送队列内存buffer满的情况下可以阻塞多长时间，超过这个时间则抛出异常
        props.put("max.block.ms", 30*1000);
        //消息压缩算法
        props.put("compression.type", "lz4");
        /**
         * 限制客户端在单个连接上能够发送的未响应请求的个数，
         * 设置此值是1 表示kafka broker在响应请求之前client不能再向同一个broker发送请求，
         * 注意:设置此参数是为了避免消息乱序
         **/
        props.put("max.in.flight.requests.per.connection", 1);
        //ack确认超时时间
        props.put("request.timeout.ms", 30*1000);
        /**
         * enable.idempotence设置为true是为了实现消息幂等（消息恰好发送一次）
         * 因为有可能因为各种网络原因或服务挂了而导致消息发送失败，然后重试，就会产生消息重复发送
         * 如果设置了true，retries将会默认设置为Integer.MAX_VALUE。
         * max.inflight.requests.per.connection这个配置将会默认为1.
         * 而且acks的配置将会默认为ALL
         **/
        props.put("enable.idempotence", true);
        /* *
         * 自定义分区函数,默认使用消息键Hash方式，为了让同一消息分发到同一分区
         *
         * 为了实现Producer的幂等性，Kafka引入了Producer ID（即PID）和Sequence Number。
         * PID。每个新的Producer在初始化的时候会被分配一个唯一的PID，这个PID对用户是不可见的。
         * Sequence Numbler。（对于每个PID，该Producer发送数据的每个<Topic, Partition>都对应一个从0开始单调递增的Sequence Number
         * Broker端在缓存中保存了这seq number，对于接收的每条消息，如果其序号比Broker缓存中序号大于1则接受它，否则将其丢弃。
         * 这样就可以实现了消息重复提交了。但是，只能保证单个Producer对于同一个<Topic, Partition>的Exactly Once语义。不能保证同一个Producer一个topic不同的partion幂等。
         *
         * 所以为了尽量保证消息幂等性，这里使用了hash分区的方式
         **/
        props.put("partitioner.class", KeyHashPartitioner.class.getCanonicalName());
        //消息键序列化类
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //消息值序列化类
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProperties kafkaProperties = KafkaConfiguration.kafkaProperties;
        if(kafkaProperties!=null){
            Map<String, Object> kafkaPropertiesMaps = kafkaProperties.buildProducerProperties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropertiesMaps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
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
        KafkaProducer producer = new KafkaProducer(props);
        return  producer;
    }


}
