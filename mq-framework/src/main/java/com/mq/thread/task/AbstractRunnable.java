package com.mq.thread.task;

import lombok.extern.slf4j.Slf4j;

/**
 * @author huacke
 * @version v1.0
 * @ClassName AbstractRunnable
 * @Description  抽象线程处理类
 */
@Slf4j
public abstract class AbstractRunnable implements Runnable{
   abstract public void doHandler();
    @Override
    public void run() {
        this.doHandler();
    }
}
