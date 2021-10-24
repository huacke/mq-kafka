package com.mq.boot.hook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import java.util.Comparator;

/**
 * 钩子
 */
@Slf4j
public abstract class BootStrapHook implements Runnable, Ordered {
    @Override
    public void run() {
    }

}
