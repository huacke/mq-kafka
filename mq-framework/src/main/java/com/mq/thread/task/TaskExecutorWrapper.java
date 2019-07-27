package com.mq.thread.task;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

class TaskExecutorWrapper implements ScheduledExecutorService, ExecutorService {

   protected final ExecutorService executorService;

   public TaskExecutorWrapper(ExecutorService executorService) {
       super();
       this.executorService = executorService;
   }

   @Override
   public void shutdown() {
       executorService.shutdown();
   }

   @Override
   public List<Runnable> shutdownNow() {
       return executorService.shutdownNow();
   }

   @Override
   public boolean isShutdown() {
       return executorService.isShutdown();
   }

   @Override
   public boolean isTerminated() {
       return executorService.isTerminated();
   }

   @Override
   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
       return executorService.awaitTermination(timeout, unit);
   }

   @Override
   public <T> Future<T> submit(Callable<T> task) {
       return executorService.submit(task);
   }

   @Override
   public <T> Future<T> submit(Runnable task, T result) {
       return executorService.submit(task, result);
   }

   @Override
   public Future<?> submit(Runnable task) {
       return executorService.submit(task);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
       return executorService.invokeAll(tasks);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
       return executorService.invokeAll(tasks, timeout, unit);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
       return (T)executorService.invokeAny(tasks);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
       return (T)executorService.invokeAny(tasks, timeout, unit);
   }

   @Override
   public void execute(Runnable command) {
       executorService.execute(TaskFactory.createRunTask(command));
   }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return ((ScheduledExecutorService)executorService).schedule(command,delay,unit);
    }
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return ((ScheduledExecutorService)executorService).schedule(callable,delay,unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return  ((ScheduledExecutorService)executorService).scheduleAtFixedRate(command,initialDelay,period,unit);
    }
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return ((ScheduledExecutorService)executorService).scheduleWithFixedDelay(command,initialDelay,delay,unit);
    }

    public CompletionService getCompletionService() {
        return new ExecutorCompletionService(executorService);
    }

    public long getTaskCount(){return ((ThreadPoolExecutor)executorService).getTaskCount();}

    public long getCompletedTaskCount(){return ((ThreadPoolExecutor)executorService).getCompletedTaskCount();};

    public BlockingQueue<Runnable> getQueue() {return ((ThreadPoolExecutor)executorService).getQueue(); }


    public void close(){

    }
}
