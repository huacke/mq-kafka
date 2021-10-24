package com.mq.worker;

import com.mq.msg.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * @author huacke
 * @version v1.0
 * @ClassName AbstractWorker
 * @Description 抽象消息处理器
 */
@Slf4j
public abstract class AbstractMsgWorker<R,M extends Message> extends AbstractWorker<R,M> implements MsgWorker<R,M> {
    @Override
    public  R onWork(M msg) {
        return work(msg);
    }

    private  R work(M message) {
        return doWork(message);
    }

}
