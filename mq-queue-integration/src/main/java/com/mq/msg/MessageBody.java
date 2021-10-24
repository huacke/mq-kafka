package com.mq.msg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mq.common.entity.BaseObject;
import lombok.Data;

/**
 * @version v1.0
 * @ClassName MessageBody
 * @Description 消息体
 */
@Data
public class MessageBody<T> extends BaseObject
{
    private static final long serialVersionUID = 5725289713751591603L;
    //消息业务主键
    private Object msgKey;
    //消息数据
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T msgData;

}
