package com.mq.consumer.kafka.scheduler;

import com.mq.thread.task.DefaultTaskExecutor;
import com.mq.thread.task.TaskExecutorService;
import com.mq.thread.task.TaskExecutorTtlWrapper;
import java.util.concurrent.*;

public class KakaOffsetLogTaskExecutor extends DefaultTaskExecutor {


   public KakaOffsetLogTaskExecutor(){
       initialize();
   }

    @Override
    protected void initialize() {
        ExecutorService executorService = new ThreadPoolExecutor(1,
                1,
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
