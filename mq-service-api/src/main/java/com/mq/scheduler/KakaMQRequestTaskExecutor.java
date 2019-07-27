package com.mq.scheduler;

import com.mq.thread.task.DefaultTaskExecutor;
import com.mq.thread.task.TaskExecutorService;
import com.mq.thread.task.TaskExecutorTtlWrapper;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

@Component
public class KakaMQRequestTaskExecutor extends DefaultTaskExecutor {
    @Override
    protected void initialize() {
        ExecutorService executorService = new ThreadPoolExecutor(1,
                1,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(20000), defaultRejectedHandler);
        taskExecutor = new TaskExecutorService(executorService, TaskExecutorTtlWrapper.class);
    }
}
