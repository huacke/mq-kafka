package com.mq.thread.task;

import java.util.concurrent.Callable;

/**
 * @author huacke
 * @version v1.0
 * @ClassName AbstractCallable
 * @Description  抽象线程处理类
 */
public abstract class  AbstractCallable<T> implements Callable<T> {
    abstract public T doHandler() throws Exception;
    @Override
    public T call() throws Exception {
        return doHandler();
    }
}
