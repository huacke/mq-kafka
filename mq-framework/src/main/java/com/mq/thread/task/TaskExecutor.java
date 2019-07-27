package com.mq.thread.task;

import java.util.List;
import java.util.concurrent.*;

public interface TaskExecutor {

    public <T> Future<T> submit(Callable<T> task);

    public Future<?> submit(Runnable task);

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);

    public CompletionService getCompletionService();

    public long getTaskCount();

    public long getCompletedTaskCount();

    public BlockingQueue<Runnable> getQueue();

    public void shutdown();

    public List<Runnable> shutdownNow();

    public boolean isShutdown();

    public boolean isTerminated();

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    public void close() throws Exception;
}
