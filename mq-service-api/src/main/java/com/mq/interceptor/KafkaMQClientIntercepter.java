
package com.mq.interceptor;

import com.mq.biz.bean.KafkaProcessMsgLog;
import com.mq.biz.bean.KafkaRequestLog;
import com.mq.biz.impl.KafkaRequestLogService;
import com.mq.biz.impl.KafkaProcessMsgLogService;
import com.mq.common.utils.StringUtils;
import com.mq.kafka.util.MsgHandleUtil;
import com.mq.msg.enums.MsgStatus;
import com.mq.msg.kafka.KafkaMessage;
import com.mq.scheduler.KakaMQRequestTaskExecutor;
import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import com.mq.thread.task.RunTask;
import com.mq.thread.task.TaskFactory;
import com.mq.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import java.util.*;


@Aspect
@Component
@Slf4j
public class KafkaMQClientIntercepter {
    /**
     * @Description 消息日志生成器
     **/
    private final DistributedIDGenerator idGenerator = new DistributedIDGenerator();

    @Autowired
    private KakaMQRequestTaskExecutor kakaMQRequestTaskExecutor;

    @Autowired
    private KafkaRequestLogService kafkaRequestLogService;

    @Autowired
    private KafkaProcessMsgLogService kafkaProcessMsgLogService;


    private static ThreadLocal<KafkaRequestLog> requestLogThreadLocal = new ThreadLocal<KafkaRequestLog>() {
        @Override
        protected KafkaRequestLog initialValue() {
            return new KafkaRequestLog();
        }
    };
    @Pointcut("@within(org.springframework.cloud.openfeign.FeignClient)&&execution(* com.mq.biz.client.api.KafkaMQClientService*.*(..))||execution(* com.mq.kafka.*.KafkaMQService.compensateSendMQ(..))")
    public void pointLog() {
    }
    @Around("pointLog()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
        Object[] args = pjp.getArgs();
        KafkaRequestLog requestLog = requestLogThreadLocal.get();
        //请求开始时间
        Date begin = new Date();
        Date end = null;
        KafkaProcessMsgLog kafkaProcessMsgLog = null;
        KafkaMessage kafkaMessage = null;
        boolean saveKafkaProcessMsgFlag =false;
        try {
            requestLog.setRequestTime(begin);
            //处理一些基本的属性
            requestLog = processrequestLogBaseInfo(pjp);
            kafkaMessage = (KafkaMessage) Arrays.stream(args).filter(it -> it instanceof KafkaMessage).findFirst().orElseGet(null);
            fillKafkaRequestLog(requestLog, kafkaMessage);

            kafkaProcessMsgLog = buildKafkaProcessMsgLog(kafkaMessage);
            saveKafkaProcessMsgFlag= saveKafKaMQProcessMessageLog(kafkaProcessMsgLog);

            //执行方法
            result = pjp.proceed();
            //返回结果处理
            processResult(result, pjp);
            handMessageStatus(result, null, requestLog);
            return result;
        } catch (Throwable e) {
            handMessageStatus(result, e, requestLog);
            processException(e, pjp);
            throw e;
        } finally {
            //返回时间
            end = new Date();
            long costTime = end.getTime() - begin.getTime();
            requestLog.setCostTime(new Long(costTime).intValue());

            saveKafkaRequestLog(requestLog);

            try{
                if (!MsgStatus.READY.getCode().equals(requestLog.getStatus())) {
                    if (kafkaProcessMsgLog != null) {
                        String id = kafkaProcessMsgLog.getId();
                        if (StringUtils.isNotEmpty(id)) {
                            Query query = new Query();
                            query.addCriteria(Criteria.where("id").is(id));
                            query.addCriteria(Criteria.where("status").ne(MsgStatus.NORMAL.getCode()));
                            Update update = new Update();
                            update.set("status", requestLog.getStatus());
                            kafkaProcessMsgLogService.update(query, update);
                        }
                    }
                    if (!saveKafkaProcessMsgFlag) {
                        if (null == kafkaProcessMsgLog) {
                            kafkaProcessMsgLog = buildKafkaProcessMsgLog(kafkaMessage);
                        }
                        kafkaProcessMsgLog.setStatus(requestLog.getStatus());
                        saveKafKaMQProcessMessageLog(kafkaProcessMsgLog);
                    }
                }
            }catch (Exception e){
                log.error("handle unNormal msg error cause by: ",e);
            }
            //释放资源
            requestLogThreadLocal.remove();
        }
    }

    private void fillKafkaRequestLog(KafkaRequestLog kafkaRequestLog, KafkaMessage message) {
        if (null == kafkaRequestLog || null == message) {
            return;
        }
        kafkaRequestLog.setId(idGenerator.generatorId(DistributedIDGenerator.IDTYPE.mq_kafka_request_log));
        kafkaRequestLog.setDigest(message.getDigest());
        kafkaRequestLog.setTopic(message.getTopic());
        kafkaRequestLog.setStatus(message.getStatus());
        kafkaRequestLog.setSourceMsgId(message.getHeader().getSourceMsgId());
        kafkaRequestLog.setMsgId(message.getHeader().getMsgId());
        kafkaRequestLog.setSourceSystem(message.getHeader().getSourceSystem());
        kafkaRequestLog.setSrcHost(message.getHeader().getSrcHost());
        kafkaRequestLog.setMsgKey(message.getMessageBody().getMsgKey());
        kafkaRequestLog.setMsgType(message.getMsgType());
        kafkaRequestLog.setMsgSubType(message.getMsgSubType());
        kafkaRequestLog.setCreatetime(new Date());
    }

    private KafkaProcessMsgLog buildKafkaProcessMsgLog(KafkaMessage kafkaMessage) {
        return kafkaProcessMsgLogService.makeLogFromMessage(kafkaMessage);
    }

    void processAttributeInfo(ProceedingJoinPoint pjp) {
        KafkaRequestLog requestLog = requestLogThreadLocal.get();
        List<Object> params = new ArrayList<>();
        try {
            Arrays.stream(pjp.getArgs()).forEach(it -> params.add(it));
            requestLog.setParam(GsonHelper.toJson(params));
        } catch (Throwable e) {
            log.error(" processAttributeInfo--error  cause by: ", e);
        }
    }

    void processResult(Object result, ProceedingJoinPoint pjp) {
        KafkaRequestLog requestLog = requestLogThreadLocal.get();
        try {
            requestLog.setCode("0");
            requestLog.setResult(GsonHelper.toJson(result));
        } catch (Throwable e) {
            log.error("processResult error  cause by: ", e);
        }
    }

    void processException(Throwable e, JoinPoint jp) throws Throwable {
        KafkaRequestLog requestlog = requestLogThreadLocal.get();
        try {
            BussinessException ex = null;
            Throwable bussinessException = null;
            if (e instanceof BussinessException) {
                ex = (BussinessException) e;
                requestlog.setCode(ex.getCode());
                requestlog.setResult(ExceptionFormatUtil.getTrace(ex, 5));
                bussinessException = ex;
            } else {
                BussinessException error = new BussinessException(ErrorCode.SYSTEM_ERROR, e);
                requestlog.setCode("1");
                requestlog.setResult(ExceptionFormatUtil.getTrace(e));
                bussinessException = error;
            }
            if (bussinessException != null) {
                throw bussinessException;
            }
        } catch (Throwable ex) {
            log.error("processException error cause by: ", e);
            throw ex;
        }
    }

    KafkaRequestLog processrequestLogBaseInfo(ProceedingJoinPoint pjp) throws Exception {
        KafkaRequestLog requestLog = requestLogThreadLocal.get();
        try {
            String ip = LocalInfo.ip;
            String method = pjp.getSignature().getName();
            processAttributeInfo(pjp);
            requestLog.setNode(ip);
            requestLog.setApi(method);
            requestLog.setCreatetime(requestLog.getCreatetime());
        } catch (Throwable e) {
            log.error("processrequestLogBaseInfo--error  cause by: ", e);
        }
        return requestLog;
    }

    //日志入库
    private void saveKafkaRequestLog(final KafkaRequestLog requestLog) {
        if (requestLog != null) {
            kakaMQRequestTaskExecutor.submitRunTask(TaskFactory.createRunTask(new RunTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        kafkaRequestLogService.saveLog(requestLog);
                    } catch (Exception e) {
                        log.error(" saveKafkaRequestLog error cause by :", e);
                    }
                }
            })));
        }
    }
    //日志入库
    private boolean saveKafKaMQProcessMessageLog(final KafkaProcessMsgLog kafkaMQProcessMessageLog) {
        if (kafkaMQProcessMessageLog != null) {
            try {
              return kafkaProcessMsgLogService.saveLog(kafkaMQProcessMessageLog);
            } catch (Exception e) {
                log.error(" savekafKaMQProcessMessageLog  error cause by :", e);
            }
        }
        return false;
    }
    private void handMessageStatus(Object result, Throwable e, KafkaRequestLog log) {
        boolean failed = MsgHandleUtil.checkSendMessageFail(result, e);
        if (failed) {
            log.setStatus(MsgStatus.FAIL.getCode());
        }
    }


}

