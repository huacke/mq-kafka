package com.mq.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @version v1.0
 * @ClassName UsualMsgType
 * @Description 常用业务消息类型定义
 */
@Getter
@AllArgsConstructor
public enum UsualMsgType implements MsgType
{
    MSG_SEND("MSG_SEND", "消息发送"),//测试用的类型，正常业务请勿使用
    ORDER_CRATE("ORDER_CRATE","下单"),
    PAYED("PAYED","支付成功");
    private String code;
    private String name;
    
    public  UsualMsgType getTypeByCode(String code){
        UsualMsgType type=null;
        UsualMsgType[] values = UsualMsgType.values();
        Optional<UsualMsgType> optional = Stream.of(values).filter(it -> it.getCode().equals(code)).findFirst();
        if(optional.isPresent()){
            type =optional.get();
        }
        return type;
    }
    
}


