package com.mq.consumer.kafka;


import com.mq.common.response.ResultHandleT;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerMsgLog;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerOffsetLog;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerMsgLogService;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerOffsetLogService;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerWorkLogService;
import com.mq.consumer.kafka.handler.KafkaMessageHandler;
import com.mq.consumer.kafka.offset.GeneralOffsetCommitCallback;
import com.mq.consumer.kafka.partition.HandleRebalance;
import com.mq.consumer.kafka.scheduler.KakaConsumerLogTaskExecutor;
import com.mq.consumer.kafka.scheduler.KakaConsumerWorkLogTaskExecutor;
import com.mq.consumer.kafka.scheduler.KakaOffsetLogTaskExecutor;
import com.mq.consumer.kafka.util.KafkaConsumerUtil;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.thread.task.TaskFactory;
import com.mq.utils.GsonHelper;
import com.mq.utils.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huacke
 * @version v1.0
 * @ClassName KafkaConsumerManager
 * @Description 消费者管理器
 */
@SuppressWarnings("ALL")
@Slf4j
@Component
public class KafkaConsumerManager implements InitializingBean {

    @Autowired
    private KafkaConsumerMsgLogService kafkaConsumerMsgLogService;
    @Autowired
    private KafkaConsumerOffsetLogService kafkaConsumerOffsetLogService;
    @Autowired
    private KafkaConsumerWorkLogService kafkaConsumerWorkLogService;

    //当前消费者消费偏移量缓存
    private static  ThreadLocal<Map<TopicPartition, OffsetAndMetadata>> currentOffsetHolder = new ThreadLocal<Map<TopicPartition, OffsetAndMetadata>>(){
        @Override
        protected Map<TopicPartition, OffsetAndMetadata> initialValue() {
            return  new ConcurrentHashMap<>();
        }
    };

    public  void  startup(List<String> topics, KafkaConsumer consumer, final AtomicBoolean running,int batchCommitSize, final KafkaMessageHandler handler) throws Exception {

        if(ObjectUtils.isEmpty(topics)){
            return;
        }
        // 当前分区偏移量
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = currentOffsetHolder.get();
        try {
            // 订阅主题，关联分区再平衡的处理器
            consumer.subscribe(topics, new HandleRebalance(consumer,currentOffsets));

            final String groupId = KafkaConsumerUtil.getGroupId(consumer);
            Thread excuteThread = Thread.currentThread();

            // 循环从Kafka拉取数据
            while (running.get() && !excuteThread.isInterrupted()) {
                    ConsumerRecords<String, String> datas = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                    if (datas.isEmpty()) {
                        continue;
                    }
                    //清除本地缓存的历史offset
                    currentOffsets.clear();

                Iterator<ConsumerRecord<String, String>> datIterator = datas.iterator();
                int dataCount=0;
                while (datIterator.hasNext()){
                    if (!running.get() || excuteThread.isInterrupted()) {
                        return;
                    }
                    ConsumerRecord<String, String> record = datIterator.next();
                    final KafkaMessage message = GsonHelper.fromJson(record.value(), KafkaMessage.class);
                    boolean vaild = KafkaUtil.checkKafkaMessage(message);
                    if (!vaild) {
                        log.error("KafkaConsumerManager kafkaMessage 消息格式有误 !!!!!!!!!!!!!!!!!!!!!!!!!!: ");
                    }
                    TopicPartition currrentTopicPartition = new TopicPartition(record.topic(), record.partition());
                    long offset = record.offset();
                    OffsetAndMetadata currentOffsetAndMetadata = new OffsetAndMetadata(offset + 1);
                    String topic = currrentTopicPartition.topic();
                    int partition = currrentTopicPartition.partition();
                    try {
                        if (vaild) {
                            ResultHandleT handleResult = null;
                           boolean rs = kafkaConsumerMsgLogService.exists(message.getDigest(), topic, groupId);
                            if (rs==false) {
                                Date beginTime = new Date();
                                try {
                                    // 处理业务逻辑
                                    handleResult = (ResultHandleT) handler.handle(message);
                                } catch (Throwable e) {
                                    log.error("KafkaConsumerManager 处理业务逻辑时出错，原因 : ", e);
                                }
                                Date endTime = new Date();
                                //记录消费者工作日志
                                saveKafkaConsumerWorkLog(handleResult, message, topic, partition, groupId, offset, beginTime, endTime);
                            } else {
                                log.warn(String.format("KafkaConsumerManager 消费者消息重复消费，已忽略===》 topic:%s,partition:%s,offset:%s ", topic, partition, offset));
                            }
                        }
                        currentOffsets.put(currrentTopicPartition, currentOffsetAndMetadata);

                        if(++dataCount%batchCommitSize==0||!datIterator.hasNext()||!running.get()){
                            //保存offset到db
                            saveOffest2Db(currentOffsets,groupId,null,false);
                            // 异步批量提交偏移量到kafka
                            consumer.commitAsync(currentOffsets, new GeneralOffsetCommitCallback(groupId, currentOffsets,false,true));
                            dataCount=0;
                        }
                    } catch (WakeupException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("KafkaConsumerManager 消费处理时出错，原因: ", e);
                    }finally {
                        //保存消费者日志
                        saveKafkaConsumerLog(message,currrentTopicPartition.topic(),currrentTopicPartition.partition(),groupId,offset);
                    }
                }

            }
        } catch (WakeupException e) {
            log.warn("KafkaConsumerManager 被通知关闭消费！");
        } catch (Exception e) {
            log.error("KafkaConsumerManager 执行消费处理时出错,原因 : ", e);
        } finally {
            try {
                syncCommitOffest(consumer,currentOffsets);
            } catch (Exception e) {
                log.error("KafkaConsumerManager 最终同步提交消费offset时出错，原因 : ", e);
            } finally {
                currentOffsetHolder.remove();
                log.warn("KafkaConsumerManager 开始最终关闭消费者...................");
                consumer.close();
                log.info("KafkaConsumerManager 关闭消费者成功！");
            }
        }
    }


    /**
     * 保存消费者工作日志
     */
    private void saveKafkaConsumerWorkLog(ResultHandleT handleResult, KafkaMessage message, String topic, int partition, String groupId, long offset, Date begin, Date end) {
        kakaConsumerWorkLogTaskExecutor.submitRunTask(TaskFactory.createRunTask(new Runnable() {
            @Override
            public void run() {
                try{
                    kafkaConsumerWorkLogService.saveKafkaConsumerWorkLog(handleResult,message,topic,partition,groupId,offset,begin,end);
                }catch (Exception e){
                    log.error("saveKafkaConsumerWorkLog error cause by:",e);
                }
            }
        }));
    }

    public void saveKafkaConsumerLog(KafkaMessage message, String topic, Integer partition, String groupId, Long offset){

        kakaConsumerLogTaskExecutor.submitRunTask(TaskFactory.createRunTask(new Runnable() {
            @Override
            public void run() {
                try{
                    //保存消费者日志
                    KafkaConsumerMsgLog kafkaConsumerMsgLog = kafkaConsumerMsgLogService.buildKafkaConsumerMsgLog(message, topic, partition, groupId, offset);
                    kafkaConsumerMsgLogService.saveKafkaConsumerMsgLog(kafkaConsumerMsgLog);
                }catch (Exception e){
                    log.error("saveKafkaConsumerLog error cause by:",e);
                }
            }
        }));
    }

    public void saveKafkaConsumerOffsetLog(String topic, Integer partition, String groupId, Long offset){
        kakaOffsetLogTaskExecutor.submitRunTask(TaskFactory.createRunTask(new Runnable() {
            @Override
            public void run() {
                try{
                    //保存消费者偏移量日志
                    KafkaConsumerOffsetLog kafkaConsumerOffsetLog = kafkaConsumerOffsetLogService.buildKafkaConsumerOffsetLog(topic, partition, groupId, offset);
                    kafkaConsumerOffsetLogService.saveKafkaConsumerOffsetLog(kafkaConsumerOffsetLog);
                }catch (Exception e){
                    log.error("saveKafkaConsumerOffsetLog error cause by:",e);
                }
            }
        }));
    }

    /**
     * 保存offset到db
     * @param currentOffsets
     * @param groupId
     * @param showLog
     */
    private void saveOffest2Db(Map<TopicPartition, OffsetAndMetadata> currentOffsets,String groupId,Exception e,boolean showLog){
        new GeneralOffsetCommitCallback(groupId,  currentOffsets ,true,showLog).onComplete(currentOffsets,e);
    }

    /**
     * 提交offset
     * @param consumer
     * @param currentOffsets
     */
    private void syncCommitOffest(KafkaConsumer consumer,Map<TopicPartition, OffsetAndMetadata> currentOffsets){
        if(CollectionUtils.isEmpty(currentOffsets)) return;
        Exception err=null;
        String groupId = KafkaConsumerUtil.getGroupId(consumer);
        try{
            consumer.commitSync(currentOffsets);
        }catch (Exception e) {
          err =e;
          throw e;
        }finally {
            saveOffest2Db(currentOffsets,groupId,err,true);
        }
    }


    private KakaConsumerLogTaskExecutor kakaConsumerLogTaskExecutor;

    private KakaOffsetLogTaskExecutor kakaOffsetLogTaskExecutor;

    private KakaConsumerWorkLogTaskExecutor kakaConsumerWorkLogTaskExecutor;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(kakaConsumerWorkLogTaskExecutor==null){
            kakaConsumerWorkLogTaskExecutor=new KakaConsumerWorkLogTaskExecutor();
        }
        if(kakaOffsetLogTaskExecutor==null){
            kakaOffsetLogTaskExecutor=new KakaOffsetLogTaskExecutor();
        }
        if(kakaConsumerLogTaskExecutor==null){
            kakaConsumerLogTaskExecutor=new KakaConsumerLogTaskExecutor();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if(kakaConsumerWorkLogTaskExecutor!=null){
                kakaConsumerWorkLogTaskExecutor.stop();
            }
            if(kakaConsumerLogTaskExecutor!=null){
                kakaConsumerLogTaskExecutor.stop();
            }
            if(kakaOffsetLogTaskExecutor!=null){
                kakaOffsetLogTaskExecutor.stop();
            }
        } catch (Exception e) {
            log.error("KafkaConsumerManager release error cause by: ", e);
        }
    }
}
