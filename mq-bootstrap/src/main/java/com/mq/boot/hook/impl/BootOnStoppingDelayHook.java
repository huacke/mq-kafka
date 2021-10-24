package com.mq.boot.hook.impl;


import com.mq.boot.hook.BootPreStopedHook;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认延迟下线钩子
 */
@Slf4j
public class BootOnStoppingDelayHook extends BootPreStopedHook {

    /**延迟下线时间*/
    private Long delayMs;

    public BootOnStoppingDelayHook(Long delayMs) {
        this.delayMs=delayMs;
    }

    @Override
    public void run() {
        if(null==delayMs){ delayMs=0L; }
        log.info("=================在服务将要下线时等待片刻 {}ms ======================",delayMs);
        synchronized (this){
            try {
                this.wait(delayMs);
            } catch (InterruptedException e) {
            }
        }
        log.info("=================等待时间结束，服务将进行下线======================");
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
