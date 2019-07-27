package com.mq.kafka.producer;

import com.mq.balance.HashLoadBalancer;
import com.mq.balance.LoadBalancer;
import com.mq.kafka.callback.SenderCallback;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.msg.kafka.TopicDef;
import com.mq.utils.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


@Slf4j
@Service
/**
 * @author huacke
 * @version v1.0
 * @ClassName KafkaProducerManager
 * @Description Kafka生产者管理器
 */
@Component
public class KafkaProducerManager implements InitializingBean, DisposableBean {

	@Autowired
	KafkaProducerBuilder kafkaProducerBuilder;

	//负载均衡器
	private HashLoadBalancer<Object,KafkaProducer> loadBalancer = new HashLoadBalancer<Object,KafkaProducer>();

	//默认超时时间15s
	private long defaultTimeOut=15*1000;
	//默认生产者实例数
	private int defaultProducerNum=1;


	public Future<RecordMetadata> sendMQSync(String topic, Serializable value) throws Exception {
		return sendMQ(topic,value,null,false);
	}

	public Future<RecordMetadata> sendMQSync(String topic, Serializable value,Long timeout) throws Exception {
	  return sendMQ(topic,value,timeout,false);
	}

	public Future<RecordMetadata> sendMQAsync(String topic, Serializable value) throws Exception {
		return sendMQ(topic,value,null,true);
	}

	private Future<RecordMetadata> sendMQ(String topic, Serializable msg,Long timeout,boolean isASync) throws Exception {
		KafkaMessage message = (KafkaMessage) msg;
		Object msgKey = message.getMessageBody().getMsgKey();
		if(null==timeout){
			timeout =defaultTimeOut;
		}
		KafkaProducer kafkaProducer = loadBalancer.get(msgKey);
		Future<RecordMetadata> future = kafkaProducer.send(
				new ProducerRecord<Object, Serializable>(topic,msgKey,
						GsonHelper.toJson(msg)),
				new SenderCallback(message));
		if(!isASync){
			try {
				RecordMetadata recordMetadata = future.get(timeout, TimeUnit.MILLISECONDS);
				new SenderCallback(message).onCompletion(recordMetadata,null);
			}catch (Exception e){
				new SenderCallback(message).onCompletion(null,e);
			}
		}
		return future;
	}

	public void shutdown() {
		log.info("开始关闭KafkaMQ消息生产者实例......................");
		try {
			Iterator<LoadBalancer.DelegateTarget<Object, KafkaProducer>> it = loadBalancer.getTargets().iterator();
			while (it.hasNext()){
				LoadBalancer.DelegateTarget<Object, KafkaProducer> itt = it.next();
				KafkaProducer producer = itt.getTarget();
				if (producer != null) {
					producer.flush();
					producer.close();
					it.remove();
				}
			}
			log.info("关闭KafkaMQ消息生产者实例成功！");
		} catch (Exception e) {
			log.error("关闭KafkaMQ消息生产者实例时出错，原因：", e);
		}
		loadBalancer =null;
	}

	public void startup() {
		log.info("开始构建KafkaMQ消息生产者实例.......................");
		try {
			IntStream.range(0, defaultProducerNum).forEach(it -> {
				try {

					KafkaProducer producer = kafkaProducerBuilder.build(null);
					loadBalancer.addTarget(producer);
					producer.partitionsFor(TopicDef.Topic.TEST.name());
				}catch (TimeoutException e){
					e.printStackTrace();
				}
				catch (Exception e) {
					log.error("构建KafkaMQ消息生产者实例时出错，原因：", e);
				}
			});
		} catch (Exception e) {
			log.error("构建KafkaMQ消息生产者实例时出错，原因：", e);
			throw e;
		}
		log.info("构建KafkaMQ消息生产者实例完毕！！！！！");
	}

	@Override
	public void destroy() throws Exception {
		shutdown();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		startup();
	}
}
