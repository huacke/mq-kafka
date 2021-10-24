package com.mq.consumer.kafka.mgr;

import com.mq.consumer.kafka.worker.DefaultKafkaMsgWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * kafka worker管理器
 */
@Slf4j
public class KafkaWorkerManager {


    /*worker注册表*/
    private static Map<String,DefaultKafkaMsgWorker> workerRegisters=new ConcurrentHashMap<>();


    /**
     * 注册worker
     * @param worker
     */
    public static void register(DefaultKafkaMsgWorker worker){
        if(worker!=null){
            workerRegisters.putIfAbsent(worker.getClass().getSimpleName(),worker);
        }
    }

    /**
     * 停止注册表中的worker
     */
    public static void stop(){

        if(CollectionUtils.isEmpty(workerRegisters))return;

        log.info(String.format("=============开始停止所有 KafkaMsgWorker  总计数量: %s ================== ",workerRegisters.size()));

        StopWatch stopWatch =new StopWatch();
        stopWatch.start();
        CountDownLatch countDownLatch =new CountDownLatch(workerRegisters.size());

        workerRegisters.entrySet().forEach(it->{

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        log.info(String.format("=============开始停止 %s ================== : ",it.getKey()));
                        it.getValue().stop();
                    }catch (Exception e){
                        log.error(String.format("=============停止 %s 出错，原因 : ",it.getKey()),e);
                    }
                    countDownLatch.countDown();
                    log.info(String.format("==================停止 %s 完成! ================== : ",it.getKey()));
                }
            }).start();

        });

        try {
            //等待容器中所有消费者关闭
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        stopWatch.stop();

        log.info(String.format("============= 停止所有 KafkaMsgWorker 完成! 花费时间: %s ms ================== ",stopWatch.getTotalTimeMillis()));

        workerRegisters=null;
    }
}
