package com.mq.thread.task;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;

public abstract class AbstractTaskExecutor {
    protected TaskExecutor taskExecutor;
    public AbstractTaskExecutor(){
    }
    public AbstractTaskExecutor(TaskExecutor taskExecutor){
        super();
        this.taskExecutor = taskExecutor;
    }
    public <T> Future<T> submitCallTask(Callable<T> task) {
        return taskExecutor.submit(task);
    }

    public Future submitRunTask(Runnable task) {
       return taskExecutor.submit(task);
    }

    public CompletionService getCompletionService() {
        return taskExecutor.getCompletionService();
    }
}
