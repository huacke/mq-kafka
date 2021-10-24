package com.mq.kafka.scheduler;

import com.mq.thread.task.DefaultTaskExecutor;
import com.mq.thread.task.TaskExecutorService;
import com.mq.thread.task.TaskExecutorTtlWrapper;
import com.mq.utils.ThreadUtil;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class KakaMQMsgHandleTaskExecutor extends DefaultTaskExecutor {

    private int threadNum =  ThreadUtil.getCpuPriorityThreadNum(null);
    @Override
    protected void initialize() {
        ExecutorService executorService = new ThreadPoolExecutor(threadNum,
                threadNum,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(20000), defaultRejectedHandler);
        taskExecutor = new TaskExecutorService(executorService, TaskExecutorTtlWrapper.class);
    }

}
