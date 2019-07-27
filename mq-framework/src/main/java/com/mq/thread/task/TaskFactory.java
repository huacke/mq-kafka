package com.mq.thread.task;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;

import java.util.concurrent.Callable;

/**
 * 注意：
 *为了保证在多线程嵌套调用父子线程数据能正确共享
 * 必须使用如下方式创建任务实例
 */
public class TaskFactory
{
    public static <T> Callable<T> createCallTask(CallTask<T> task){ return TtlCallable.get(task); }

    public static Runnable createRunTask(RunTask task){ return TtlRunnable.get(task); }

    public static <T> Callable<T> createCallTask(Callable<T> task) {
        return TtlCallable.get(new CallTask<T>());
    }

    public static Runnable createRunTask(Runnable task) {
        return TtlRunnable.get(new RunTask(task));
    }
}
