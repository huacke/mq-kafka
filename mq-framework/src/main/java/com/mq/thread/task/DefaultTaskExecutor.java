package com.mq.thread.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public abstract class  DefaultTaskExecutor extends AbstractTaskExecutor implements InitializingBean, DisposableBean {

    private String classSimpleName = this.getClass().getSimpleName();

    //任务线程池线程编号
    protected final AtomicInteger taskThreadNo = new AtomicInteger(1);

    protected ThreadFactory defaultThreadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(this.getClass().getSimpleName()+"-ThreadPool-thread-"+taskThreadNo.incrementAndGet());
            return thread;
        }
    };

    /**
     * @Description 默认的任务拒绝策略
     **/
  protected   RejectedExecutionHandler defaultRejectedHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                executor.submit(r);
            }
        }
    };
    /**
     * @Description 初始化资源
     **/
    protected  abstract void initialize();
    /**
     * @Description 释放资源
     **/
    protected  void release(){};

    @Override
    public void afterPropertiesSet() throws Exception {
       initialize();
    }
    @Override
    public void destroy() throws Exception {
        try {
            release();
        } catch (Exception e) {
        }
        close();
    }
    /**
     * @Description 回收线程池资源
     **/
    protected void close() throws Exception {
        //释放消费者线程池资源
        log.info(String.format("%s 开始关闭线程池资源..................... ", classSimpleName));
        try {
            if (taskExecutor != null && !taskExecutor.isTerminated()) {
                taskExecutor.shutdown();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
                int retryCount = 100;
                int retry = 0;
                //循环检查线程池是否已经关闭，达到一定次数，然后再等待一段时间关闭线程池
                while (!taskExecutor.isTerminated() && retryCount > 0) {
                    retry++;
                    long taskCount = taskExecutor.getTaskCount();
                    long completedTaskCount = taskExecutor.getCompletedTaskCount();
                    long notCompletedTaskCount = taskCount-completedTaskCount;
                    log.info(String.format("%s 正在重试等待线程资源关闭,重试次数：%s,队列剩余容量：%s，当前未完成任务数：%s", classSimpleName, retry,taskExecutor.getQueue().remainingCapacity(),notCompletedTaskCount));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    retryCount--;
                }
                if(!taskExecutor.isTerminated()){
                    log.info(String.format("%s 重试等待%s线程资源超时，准备使用等待超时释放策略 .....", classSimpleName));
                    //重试多次失败，等待一段时间，关闭线程池
                    taskExecutor.awaitTermination(2, TimeUnit.MINUTES);
                }
            }
        } catch (Exception e) {
            log.info(String.format("%s 释放线程池资源时出错！原因 : ", classSimpleName), e);
        }
        log.info(String.format("%s  释放线程池资源完毕！  ", classSimpleName));
    }


}
