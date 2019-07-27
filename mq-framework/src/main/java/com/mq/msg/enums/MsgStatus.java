package com.mq.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @version v1.0
 * @ClassName SystemID
 * @Description 消息状态定义
 */

@AllArgsConstructor
@Getter
public enum MsgStatus
{
    READY("READY", "就绪"),//待发送

    NORMAL("NORMAL", "正常"),//发送成功

    FAIL("FAIL", "失败"),//发送失败

    COMPENSATE("COMPENSATE", "补偿状态"),//消息重试

    DEAD("DEAD", "死亡状态，多次重试无法操作成功");//重试多次失败，标记消息死亡

    private String code;
    private String name;

    public static MsgStatus getTypeByCode(String code){
        MsgStatus type=null;
        MsgStatus[] values = MsgStatus.values();
        Optional<MsgStatus> optional = Stream.of(values).filter(it -> it.getCode().equals(code)).findFirst();
        if(optional.isPresent()){
            type =optional.get();
        }
        return type;
    }
}
