package com.mq.consumer.kafka.scheduler;

import com.mq.thread.task.DefaultTaskExecutor;
import com.mq.thread.task.TaskExecutorService;
import com.mq.thread.task.TaskExecutorTtlWrapper;
import com.mq.utils.ThreadUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class KakaConsumerWorkLogTaskExecutor extends DefaultTaskExecutor {

    private int threadNum =  ThreadUtil.getCpuPriorityThreadNum(null);

    public KakaConsumerWorkLogTaskExecutor(){
        initialize();
    }

    @Override
    protected void initialize() {
        ExecutorService executorService = new ThreadPoolExecutor(threadNum,
                threadNum,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(20000),defaultThreadFactory, defaultRejectedHandler);
        taskExecutor = new TaskExecutorService(executorService, TaskExecutorTtlWrapper.class);
    }


    @Override
    public void destroy() throws Exception {
        //这里重载销毁方法是为了手动控制资源释放
    }

    public void stop() throws Exception {
        super.destroy();
    }

}
