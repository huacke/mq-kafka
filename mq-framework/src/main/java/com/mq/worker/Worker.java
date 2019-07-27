package com.mq.worker;

/**
 * @author huacke
 * @version v1.0
 * @ClassName Worker
 * @Description 工作处理器接口定义
 */
public interface Worker<R,T> {
     R onWork(T data);
}
