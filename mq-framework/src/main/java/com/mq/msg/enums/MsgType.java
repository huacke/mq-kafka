package com.mq.msg.enums;

/**
 * @version v1.0
 * @ClassName MsgType
 * @Description 消息类型接口定义
 */

public interface MsgType
{
   public String getCode();
   public String getName();
   public  MsgType getTypeByCode(String code);
}
