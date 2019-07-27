package com.mq.thread.task;

import java.util.List;
import java.util.concurrent.*;

public class TaskExecutorService implements TaskExecutor {

    private  TaskExecutorWrapper taskExecutorWrapper;

    private TaskExecutorService() {
    }

    public TaskExecutorService(ExecutorService executorService) {
        super();
        this.taskExecutorWrapper = new TaskExecutorWrapper(executorService);
    }
    public TaskExecutorService(ExecutorService executorService,Class<? extends TaskExecutorWrapper> type) {
        super();
        try {
            taskExecutorWrapper = (TaskExecutorWrapper) type.getDeclaredConstructor(ExecutorService.class).newInstance(executorService);
        } catch (Exception e) {
        }
    }
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return taskExecutorWrapper.submit(TaskFactory.createCallTask(task));
    }
    @Override
    public Future<?> submit(Runnable task) {
        return taskExecutorWrapper.submit(TaskFactory.createRunTask(task));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return taskExecutorWrapper.schedule(command,delay,unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return taskExecutorWrapper.schedule(callable,delay,unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return taskExecutorWrapper.scheduleAtFixedRate(command,initialDelay,period,unit);
    }
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return taskExecutorWrapper.scheduleWithFixedDelay(command,initialDelay,delay,unit);
    }

    @Override
    public CompletionService getCompletionService() {
        return taskExecutorWrapper.getCompletionService();
    }

    @Override
    public long getTaskCount() {
        return taskExecutorWrapper.getTaskCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return taskExecutorWrapper.getCompletedTaskCount();
    }

    @Override
    public BlockingQueue<Runnable> getQueue() {
        return taskExecutorWrapper.getQueue();
    }

    @Override
    public void shutdown() {
        taskExecutorWrapper.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return taskExecutorWrapper.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return taskExecutorWrapper.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return taskExecutorWrapper.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return taskExecutorWrapper.awaitTermination(timeout,unit);
    }

    @Override
    public void close() throws Exception {
    }
}
