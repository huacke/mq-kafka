package com.mq.kafka.scheduler;


import com.mq.biz.bean.KafkaDeadedMsgLog;
import com.mq.biz.bean.KafkaMsgLog;
import com.mq.biz.bean.KafkaProcessMsgLog;
import com.mq.biz.impl.KafkaDeadedMsgLogService;
import com.mq.biz.impl.KafkaMsgLogService;
import com.mq.biz.impl.KafkaProcessMsgLogService;
import com.mq.common.entity.EntiytList;
import com.mq.common.entity.page.Page;
import com.mq.common.utils.ConvertUtils;
import com.mq.kafka.biz.api.KafkaMQService;
import com.mq.kafka.producer.KafkaProducerManager;
import com.mq.msg.enums.MsgStatus;
import com.mq.common.sys.SystemID;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.redis.RedisKey;
import com.mq.redis.lock.GlobalLock;
import com.mq.redis.lock.GlobalLockRedisFactory;
import com.mq.utils.GsonHelper;
import com.mq.worker.AbstractWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;
import java.util.Date;
import java.util.List;

/**
 * @Description kafka消息补偿，消息日志同步定时任务
 **/
@SuppressWarnings("ALL")
@Component
@Configuration
@EnableScheduling
@Slf4j
public class KakaMQMessageTask extends AbstractWorker<Void,Object> {

    @Autowired
    private KafkaProducerManager kafkaProducerManager; //注入保证生产者服务已经初始化
    @Autowired
    private GlobalLockRedisFactory globalLockRedisFactory;
    @Autowired
    private KafkaMsgLogService kafkaMsgLogService;
    @Autowired
    private KafkaProcessMsgLogService kafkaProcessMsgLogService;
    @Autowired
    private KafkaDeadedMsgLogService kafkaDeadedMsgLogService;
    @Autowired
    private KafkaMQService kafkaMQService;

    @Scheduled(cron = "0/2 * * * * ?")
    public void work() {
        onWork(null);
    }

    @Override
    public Void onWork(Object data) {

        log.info("===========开始进行消息补偿任务========");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //加分布式锁，控制多实例同时操作
        GlobalLock lock = globalLockRedisFactory.getLock(RedisKey.getKAFKA_MQ_MESSAGE_LOG_LOCK_KEY());
        try {
            boolean acquire = lock.tryLock(500);
            if (!acquire) {
                log.info("===========已有实例在运行补偿任务，忽略此次任务========");
                return null;
            }
            Page<KafkaProcessMsgLog> page = new Page<KafkaProcessMsgLog>(1000);
            EntiytList<KafkaProcessMsgLog> mqMessageLogs = null;
            do {
                if(Thread.currentThread().isInterrupted()){
                    return null;
                }
                mqMessageLogs = queryKafkaProcessMsgLogs(page, null);
                {
                    handeMsgLog(mqMessageLogs);
                }
                page.setPageNum(page.getPageNum() + 1);
            } while (mqMessageLogs != null && !ObjectUtils.isEmpty(mqMessageLogs.getRows()));
        } finally {
            lock.unlock();
        }
        stopWatch.stop();
        log.info(String.format("===========消息补偿任务结束，总共耗时 %s ms：========", stopWatch.getTotalTimeMillis()));
        return null;
    }

    /**
     * 消息遍历处理
     * @param mqMessageLogs
     */
    private void handeMsgLog( EntiytList<KafkaProcessMsgLog> mqMessageLogs){
        if (mqMessageLogs!=null&&!CollectionUtils.isEmpty(mqMessageLogs.getRows())) {
            List<KafkaProcessMsgLog> rows = mqMessageLogs.getRows();
            rows.forEach(it -> {
                String onStatus = it.getStatus();
                try {
                    if (MsgStatus.FAIL.getCode().equals(onStatus)
                            || MsgStatus.COMPENSATE.getCode().equals(onStatus)
                            || MsgStatus.READY.getCode().equals(onStatus)) {
                        if (shouldReSend(it)) {
                            doReSend(it);
                        }
                        if (checkMessageDeaded(it)) {
                            saveDeadedMessage(it);
                        }
                    } else if (MsgStatus.DEAD.getCode().equals(onStatus)) {
                        saveDeadedMessage(it);
                    } else if (MsgStatus.NORMAL.getCode().equals(onStatus)) {
                        processNormalMessage(it);
                    }
                } catch (Exception e) {
                    log.error("KakaMQMessageTask onWork error cause by :", e);
                }
            });
        }
    }

    /**
     * @Description 分页查询正在处理中的消息日志
     **/
    private EntiytList<KafkaProcessMsgLog> queryKafkaProcessMsgLogs(Page page, Query query) {
        EntiytList<KafkaProcessMsgLog> mqMessageLogs = null;
        try {
            mqMessageLogs = kafkaProcessMsgLogService.findByPage(page, null);
        } catch (Exception e) {
            log.error("queryKafkaProcessMsgLogs error cause by :", e);
        }
        return mqMessageLogs;
    }

    /**
     * @Description 检查消息是否死亡
     **/
    private boolean checkMessageDeaded(KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        boolean deaded = false;
        String onStatus = kafkaMQProcessMessageLog.getStatus();
        Date createtime = kafkaMQProcessMessageLog.getCreatetime();
        Date overtime = DateUtils.addMinutes(createtime, 15);
        Date currentTime = new Date();
        Integer retrytimes = kafkaMQProcessMessageLog.getRetrytimes();
        if (MsgStatus.NORMAL.getCode().equals(onStatus)) {
            return false;
        }
        if (null == retrytimes) {
            retrytimes = 0;
        }
        if (MsgStatus.FAIL.getCode().equals(onStatus) || MsgStatus.COMPENSATE.getCode().equals(onStatus)) {
            //重试次数达到阈值或者确认消息超时，则消息进入死亡状态
            if (retrytimes > 5 || currentTime.after(overtime)) {
                deaded = true;
            }
        } else if (MsgStatus.READY.getCode().equals(onStatus)) {
            //待发送的消息，超过一定时间没有得到消息队列的确认，消息标记死亡
            if (currentTime.after(overtime)) {
                deaded = true;
            }
        }
        return deaded;
    }

    /**
     * @Description 是否可重发消息
     **/
    private boolean shouldReSend(KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        boolean shouldReSend = false;
        boolean deaded = checkMessageDeaded(kafkaMQProcessMessageLog);
        String onStatus = kafkaMQProcessMessageLog.getStatus();
        if (MsgStatus.FAIL.getCode().equals(onStatus)) {
            if (!deaded) {
                shouldReSend = true;
            }
        } else if (MsgStatus.READY.getCode().equals(onStatus)) {
            Date createtime = kafkaMQProcessMessageLog.getCreatetime();
            Date currentTime = new Date();
            //待发送的消息，指定时间内未成功发送kafKa队列，消息重发
            Date overtime = DateUtils.addSeconds(createtime, 6*60);
            if (!deaded && currentTime.after(overtime)) {
                shouldReSend = true;
            }
        }
        return shouldReSend;
    }

    /**
     * @Description 补偿机制，重新发送消息
     **/
    private boolean doReSend(KafkaProcessMsgLog kafkaMQProcessMessageLog) {

        boolean sucessFlag = true;
        String message = kafkaMQProcessMessageLog.getMessage();
        KafkaMessage kafkaMessage = GsonHelper.fromJson(message, KafkaMessage.class);
        if (kafkaMessage != null) {
            Integer retrytimes = ConvertUtils.toInteger(kafkaMQProcessMessageLog.getRetrytimes(), 0).intValue();
            kafkaMQProcessMessageLog.setRetrytimes(++retrytimes);
            try {
                //重发前再查询一下消息状态，如果是已发送，直接返回
                KafkaProcessMsgLog currentKafkaProcessMsgLog = kafkaProcessMsgLogService.findById(kafkaMQProcessMessageLog.getId());
                if (currentKafkaProcessMsgLog != null) {
                    String status = currentKafkaProcessMsgLog.getStatus();
                    if (MsgStatus.NORMAL.getCode().equals(status)) {
                        return false;
                    }
                    try {
                        kafkaMQProcessMessageLog.setStatus(MsgStatus.COMPENSATE.getCode());
                        kafkaMQProcessMessageLog.setUpdateTime(new Date());
                        kafkaProcessMsgLogService.saveLog(kafkaMQProcessMessageLog);
                    } catch (Exception e) {
                        log.error("doResend KafkaMQMessage save kafkaMQProcessMessageLog error cause by :", e);
                    }
                    kafkaMessage.setStatus(MsgStatus.COMPENSATE.getCode());
                    kafkaMessage.getHeader().setSourceSystem(SystemID.MQ_SERVICE.name());
                    kafkaMQService.compensateSendMQ(kafkaMessage);
                }
            } catch (Exception e) {
                log.error("doResend KafkaMQMessage error cause by :", e);
            }
        }
        return sucessFlag;
    }

    /**
     * @Description 处理已经成功发送到消息队列的消息
     **/
    private void processNormalMessage(KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        boolean successFlag = false;
        try {
            //保存消息到全量消息表
            successFlag = saveKafKaMQMessageLog(kafkaMQProcessMessageLog);
            if (successFlag) {
                //删除已经成功处理的消息
                deleteProcessMessage(kafkaMQProcessMessageLog);
            }
        } catch (Exception e) {
            log.error("processNormalMessage error cause by :", e);
        }
    }

    /**
     * @Description 保存消息日志到全量消息表
     **/
    private boolean saveKafKaMQMessageLog(KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        KafkaMsgLog kafkaMQMessageLog = new KafkaMsgLog();
        try {
            BeanUtils.copyProperties(kafkaMQProcessMessageLog, kafkaMQMessageLog);
            kafkaMQMessageLog.setUpdateTime(new Date());
            return kafkaMsgLogService.saveLog(kafkaMQMessageLog);
        } catch (Exception e) {
            log.error("savekafKaMQMessageLog error cause by :", e);
        }
        return false;
    }

    /**
     * @Description 死亡消息移入死信队列
     **/
    private boolean saveDeadedMessage(KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        boolean successFlag = false;
        kafkaMQProcessMessageLog.setStatus(MsgStatus.DEAD.getCode());
        kafkaMQProcessMessageLog.setDeaded(1);
        KafkaDeadedMsgLog kafkaMQDeadedMessageLog = new KafkaDeadedMsgLog();
        try {
            BeanUtils.copyProperties(kafkaMQProcessMessageLog, kafkaMQDeadedMessageLog);
            kafkaMQDeadedMessageLog.setUpdateTime(new Date());
            successFlag = kafkaDeadedMsgLogService.saveLog(kafkaMQDeadedMessageLog);
            if (successFlag) {
                deleteProcessMessage(kafkaMQProcessMessageLog);
            }
        } catch (Exception e) {
            log.error("saveDeadedMessage error cause by :", e);
        }
        return successFlag;
    }

    /**
     * @param kafkaMQProcessMessageLog
     * @return
     * @Description 删除已经处理完毕的消息
     **/
    private void deleteProcessMessage(KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        try {
            kafkaProcessMsgLogService.deleteById(kafkaMQProcessMessageLog.getId());
        } catch (Exception e) {
            log.error("deleteProcessMessage error cause by :", e);
        }
    }


}
