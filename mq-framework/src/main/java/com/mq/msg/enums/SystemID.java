package com.mq.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @version v1.0
 * @ClassName SystemID
 * @Description 消息来源系统定义
 */

@AllArgsConstructor
@Getter
public enum SystemID
{
    SERVICE("SERVICE","默认服务"),
    MQ_SERVICE("MQ_SERVICE","MQ生产服务");

    private String code;
    private String name;
    public static SystemID getSystemByCode(String code){
        SystemID system=null;
        SystemID[] values = SystemID.values();
        Optional<SystemID> optional = Stream.of(values).filter(it -> it.getCode().equals(code)).findFirst();
        if(optional.isPresent()){
            system =optional.get();
        }
        return system;
    }
}
