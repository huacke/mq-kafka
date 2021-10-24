package com.mq.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @version v1.0
 * @ClassName UsualMsgSubType
 * @Description 常用业务消息子类型定义
 */
@Getter
@AllArgsConstructor
public enum UsualMsgSubType implements MsgSubType
{
    DEFAULT("DEFAULT", "默认");
    private String code;
    private String name;

    public UsualMsgSubType getTypeByCode(String code){
        UsualMsgSubType type=null;
        UsualMsgSubType[] values = UsualMsgSubType.values();
        Optional<UsualMsgSubType> optional = Stream.of(values).filter(it -> it.getCode().equals(code)).findFirst();
        if(optional.isPresent()){
            type =optional.get();
        }
        return type;
    }

}

