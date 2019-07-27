package com.mq.worker;


import com.mq.msg.Message;

/**
 * @author huacke
 * @version v1.0
 * @ClassName MsgWorker
 * @Description 消息工作处理器
 */
public interface MsgWorker<R,M extends Message> extends Worker<R,M> {
    /**
     * 执行处理
     * @param message 消息对象
     * @return　T
     */
     R  doWork(M message);
}
