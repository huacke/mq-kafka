package com.mq.boot.hook;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认的启动完成回调处理器
 */
@Slf4j
public class BootStrapUpHook extends BootStrapHook{


    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
