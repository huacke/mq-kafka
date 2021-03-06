package com.mq.msg.enums;

/**
 * @version v1.0
 * @ClassName MsgType
 * @Description 消息子类型接口定义
 */
public interface MsgSubType {
	public String getCode();
	public String getName();
	public MsgSubType getTypeByCode(String code);
}
