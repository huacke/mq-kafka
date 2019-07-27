package com.mq.msg;

/**
 * @version v1.0
 * @ClassName BaseMessage
 * @Description 基础消息对象
 */

import com.mq.entity.BaseObject;
import lombok.Data;

@Data
public class BaseMessage<T> extends BaseObject
{
    private static final long serialVersionUID = -1020933674256873118L;
    //消息主类型
    private String msgType;
    //消息子类型
    private String msgSubType;
    //消息状态
    private String status;

    private MessageHeader header = new MessageHeader();

    private MessageBody<T> messageBody = new MessageBody<T>();
}
