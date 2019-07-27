package com.mq.consumer.kafka;


import com.mq.common.exception.BussinessException;
import com.mq.common.response.ResultHandleT;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerMsgLog;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerOffsetLog;
import com.mq.consumer.kafka.biz.bean.KafkaConsumerWorkLog;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerMsgLogService;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerOffsetLogService;
import com.mq.consumer.kafka.biz.impl.KafkaConsumerWorkLogService;
import com.mq.consumer.kafka.handler.KafkaMessageHandler;
import com.mq.consumer.kafka.offset.GeneralOffsetCommitCallback;
import com.mq.consumer.kafka.partition.HandleRebalance;
import com.mq.consumer.kafka.scheduler.KakaOffsetLogTaskExecutor;
import com.mq.consumer.kafka.util.KafkaConsumerUtil;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.thread.task.TaskFactory;
import com.mq.utils.ExceptionFormatUtil;
import com.mq.utils.GsonHelper;
import com.mq.utils.KafkaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
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
@Slf4j
@Component
public class KafkaConsumerManager {
    @Autowired
    private KafkaConsumerMsgLogService kafkaConsumerMsgLogService;
    @Autowired
    private KafkaConsumerOffsetLogService kafkaConsumerOffsetLogService;
    @Autowired
    private KafkaConsumerWorkLogService kafkaConsumerWorkLogService;
    @Autowired
    private KakaOffsetLogTaskExecutor kakaOffsetLogTaskExecutor;

    private byte[] kafkaConsumerMsgLogLock  = new byte[0];
    private byte[] kafkaConsumerOffsetLogLock = new byte[0];

    //当前消费者消费偏移量缓存
    public static  ThreadLocal<Map<TopicPartition, OffsetAndMetadata>> currentOffsetHolder = new ThreadLocal<Map<TopicPartition, OffsetAndMetadata>>(){
        @Override
        protected Map<TopicPartition, OffsetAndMetadata> initialValue() {
            return  new HashMap();
        }
    };

    public  void startup(List<String> topics, KafkaConsumer consumer, final AtomicBoolean running, final KafkaMessageHandler handler) throws Exception {
        if(ObjectUtils.isEmpty(topics)){
            return;
        }
        // 当前分区偏移量
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();
        try {
            // 订阅主题，关联分区再平衡的处理器
            consumer.subscribe(topics, new HandleRebalance(consumer,Thread.currentThread()));
            final String groupId = KafkaConsumerUtil.getGroupId(consumer);
            // 循环从Kafka拉取数据
            Thread excuteThread = Thread.currentThread();
            while (running.get()&&!excuteThread.isInterrupted()) {
                try {
                    ConsumerRecords<String, String> datas = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                    if (datas.isEmpty()) {
                        continue;
                    }
                    //清除本地缓存的历史offset
                    currentOffsets.clear();
                    datas.forEach(record -> {
                        if (!running.get() || excuteThread.isInterrupted()) {
                            return;
                        }
                        final KafkaMessage message = GsonHelper.fromJson(record.value(), KafkaMessage.class);
                        if (null == message) {
                            log.error("KafkaConsumerManager kafkaMessage 为空 !!!!!!!!!!!!!!!!!!!!!!!!!!: ");
                            return;
                        } else {
                            boolean vaild = KafkaUtil.checkKafkaMessage(message);
                            if (!vaild) {
                                log.error("KafkaConsumerManager kafkaMessage 消息格式有误 !!!!!!!!!!!!!!!!!!!!!!!!!!: ");
                            }
                        }
                        TopicPartition currrentTopicPartition = new TopicPartition(record.topic(), record.partition());
                        long offset = record.offset();
                        OffsetAndMetadata currentOffsetAndMetadata = new OffsetAndMetadata(offset + 1);
                        String topic = currrentTopicPartition.topic();
                        int partition = currrentTopicPartition.partition();
                        KafkaConsumerMsgLog kafkaConsumerMsgLog = null;
                        try {
                            try {
                                kafkaConsumerMsgLog = kafkaConsumerMsgLogService.queryKafkaConsumerMsgLog(message.getDigest(), topic, partition, groupId);
                            } catch (Exception e) {
                                log.error("KafkaConsumerManager query kafkaConsumerMsgLog 出错，原因 : ", e);
                            }
                            // 处理业务逻辑
                            ResultHandleT handleResult = null;
                            if (null == kafkaConsumerMsgLog) {
                                Date beginTime = new Date();
                                try {
                                    handleResult = (ResultHandleT) handler.handle(message);
                                } catch (Throwable e) {
                                    log.error("KafkaConsumerManager 处理业务逻辑时出错，原因 : ", e);
                                } finally {
                                    currentOffsets.put(currrentTopicPartition, currentOffsetAndMetadata);
                                    currentOffsetHolder.get().putAll(currentOffsets);
                                }
                                Date endTime = new Date();
                                saveKafkaConsumerWorkLog(handleResult, message, topic, partition, groupId, offset, beginTime, endTime);
                            } else {
                                log.warn(String.format("KafkaConsumerManager 消费者消息重复消费，已忽略===》 topic:%s,partition:%s,offset:%s ", topic, partition, offset));
                            }
                            // 先异步提交偏移量
                            consumer.commitAsync(currentOffsets, new GeneralOffsetCommitCallback(consumer, message, currentOffsets));
                        } catch (WakeupException e) {
                            if (running.get())
                                throw e;
                        } catch (Exception e) {
                            log.error("KafkaConsumerManager 消费处理时出错，原因: ", e);
                        } finally {
                            try {
                                consumer.commitSync(currentOffsets);
                                if (currentOffsets.containsKey(currrentTopicPartition)) {
                                    saveLog(message, topic, partition, groupId, currentOffsetAndMetadata.offset());
                                }
                                currentOffsets.remove(currrentTopicPartition);
                            } catch (WakeupException e) {
                                if (running.get())
                                    throw e;
                            } catch (Exception e) {
                                log.error("KafkaConsumerManager 同步提交offset时出错，原因: ", e);
                            }
                        }
                    });
                } catch (WakeupException e) {
                    if (running.get())
                        throw e;
                }
            }
        } catch (WakeupException e) {
            log.info("KafkaConsumerManager 被通知关闭消费！");
        } catch (Exception e) {
            log.error("KafkaConsumerManager 执行消费处理时出错,原因 : ", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } catch (Exception e) {
                log.error("KafkaConsumerManager 最终同步提交消费offset时出错，原因 : ", e);
            } finally {
                log.info("KafkaConsumerManager 开始最终关闭消费者...................");
                consumer.close();
                log.info("KafkaConsumerManager 关闭消费者成功！");
            }
            currentOffsetHolder.remove();
        }
    }

    /**
     * 保存消费者工作日志
     */
    private void saveKafkaConsumerWorkLog(ResultHandleT handleResult, KafkaMessage message, String topic, int partition, String groupId, long offset, Date begin, Date end) {
        try {
            KafkaConsumerWorkLog kafkaConsumerWorkLog = kafkaConsumerWorkLogService.buildKafkaConsumerWorkLog(message, topic, partition, groupId, offset);
            kafkaConsumerWorkLog.setRequestTime(begin);
            kafkaConsumerWorkLog.setResponseTime(end);
            if (begin != null && end != null) {
                long costTime = end.getTime() - begin.getTime();
                kafkaConsumerWorkLog.setCostTime((int) costTime);
            }
            if (handleResult != null) {
                Throwable error = handleResult.getE();
                if (error != null) {
                    if (error instanceof BussinessException) {
                        BussinessException err = (BussinessException) error;
                        kafkaConsumerWorkLog.setCode(err.getCode());
                    } else {
                        kafkaConsumerWorkLog.setCode("1");
                    }
                    kafkaConsumerWorkLog.setResult(ExceptionFormatUtil.getTrace(error, 5));
                } else {
                    kafkaConsumerWorkLog.setCode("0");
                    kafkaConsumerWorkLog.setResult(GsonHelper.toJson(handleResult.getData()));
                }
            }
            kafkaConsumerWorkLogService.saveLog(kafkaConsumerWorkLog);
        } catch (Exception e) {
            log.error("KafkaConsumerManager saveKafkaConsumerMsgLog error cause by: ", e);
        }
    }

    /**
     * @Description 保存消费日志
     **/
    private  boolean saveKafkaConsumerMsgLog(KafkaConsumerMsgLog kafkaConsumerMsgLog){
        try {
            synchronized (kafkaConsumerMsgLogLock){
                return kafkaConsumerMsgLogService.saveLog(kafkaConsumerMsgLog);
            }
        }
        catch (Exception e){
            log.error("KafkaConsumerManager saveKafkaConsumerMsgLog error cause by: ", e);
        }
        return false;
    }


    /**
     * @Description 保存消费偏移量日志
     **/
    private  boolean  saveKafkaConsumerOffsetLog(KafkaConsumerOffsetLog kafkaConsumerOffsetLog){
        try {
            synchronized (kafkaConsumerOffsetLogLock){
                return kafkaConsumerOffsetLogService.saveLog(kafkaConsumerOffsetLog);
            }

        }
        catch (Exception e){
            log.error("KafkaConsumerManager saveKafkaConsumerOffsetLog error cause by: ", e);
        }
        return false;
    }

    public void saveLog(KafkaMessage message, String topic, Integer partition, String groupId, Long offset){

        kakaOffsetLogTaskExecutor.submitRunTask(TaskFactory.createRunTask(new Runnable() {
            @Override
            public void run() {
                //保存消费者偏移量日志
                KafkaConsumerOffsetLog kafkaConsumerOffsetLog = kafkaConsumerOffsetLogService.buildKafkaConsumerOffsetLog(topic, partition, groupId, offset);
                saveKafkaConsumerOffsetLog(kafkaConsumerOffsetLog);
                //保存消费者日志
                KafkaConsumerMsgLog kafkaConsumerMsgLog = kafkaConsumerMsgLogService.buildKafkaConsumerMsgLog(message, topic, partition, groupId, offset);
                saveKafkaConsumerMsgLog(kafkaConsumerMsgLog);
            }
        }));
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            kakaOffsetLogTaskExecutor.stop();
        } catch (Exception e) {
            log.error("KafkaConsumerManager release error cause by: ", e);
        }
    }


}
