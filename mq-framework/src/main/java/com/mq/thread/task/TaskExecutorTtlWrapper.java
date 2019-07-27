package com.mq.thread.task;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class TaskExecutorTtlWrapper  extends TaskExecutorWrapper {

   public TaskExecutorTtlWrapper(ExecutorService executorService) {
       super(executorService);
   }

   @Override
   public void shutdown() {
       super.shutdown();
   }

   @Override
   public List<Runnable> shutdownNow() {
       return super.shutdownNow();
   }

   @Override
   public boolean isShutdown() {
       return super.isShutdown();
   }

   @Override
   public boolean isTerminated() {
       return super.isTerminated();
   }

   @Override
   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
       return super.awaitTermination(timeout,unit);
   }

    private <T> Callable<T> wrappCalcCallTask(Callable<T> task) {
        if (task instanceof TtlCallable) {
            return task;
        }
        return TaskFactory.createCallTask(task);
    }

    private <T> Runnable wrappCalcRunTask(Runnable task) {
        if (task instanceof TtlRunnable) {
            return task;
        }
        return TaskFactory.createRunTask(task);
    }

   @Override
   public <T> Future<T> submit(Callable<T> task) {
      return super.submit(wrappCalcCallTask(task));
   }

   @Override
   public <T> Future<T> submit(Runnable task, T result) {
       return super.submit(wrappCalcRunTask(task), result);
   }

   @Override
   public Future<?> submit(Runnable task) {
       return super.submit(wrappCalcRunTask(task));
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
       Collection calcCallTasks = new ArrayList<>();
       for (Callable callable : tasks) {
           calcCallTasks.add(wrappCalcCallTask(callable));
       }
       return super.invokeAll(calcCallTasks);
   }

   @Override
   public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
       Collection calcCallTasks = new ArrayList<>();
       for (Callable callable : tasks) {
           calcCallTasks.add(wrappCalcCallTask(callable));
       }
       return super.invokeAll(calcCallTasks, timeout, unit);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
       Collection calcCallTasks = new ArrayList<>();
       for (Callable callable : tasks) {
           calcCallTasks.add(wrappCalcCallTask(callable));
       }
       return (T)super.invokeAny(calcCallTasks);
   }

   @Override
   public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
       Collection calcCallTasks = new ArrayList<>();
       for (Callable callable : tasks) {
           calcCallTasks.add(wrappCalcCallTask(callable));
       }
       return (T)super.invokeAny(calcCallTasks, timeout, unit);
   }

   @Override
   public void execute(Runnable command) {
       super.execute(wrappCalcRunTask(command));
   }

   public CompletionService getCompletionService() {
       return super.getCompletionService();
   }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return super.schedule(wrappCalcRunTask(command),delay,unit);
    }
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return super.schedule(wrappCalcCallTask(callable),delay,unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return  super.scheduleAtFixedRate(wrappCalcRunTask(command),initialDelay,period,unit);
    }
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrappCalcRunTask(command),initialDelay,delay,unit);
    }
    public void close(){

    }
}
