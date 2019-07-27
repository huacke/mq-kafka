package com.mq.common.response.code;

public interface ResponseConst {
	String  SUCCESS_CODE="0";//(0), //系统 层面
	String	OPERATION_ADD_SUCCESS_CODE="010";//(010),
	String	OPERATION_DELETE_SUCCESS_CODE="011";//(011),
	String	OPERATION_UPDATE_SUCCESS_CODE="012";//(012),
	/**
	 * 状态码标识
	 */
	String CODE= "code";
	/**
	 * 提示信息标识
	 */
	String MESSAGE= "message";
	/**
	 * 数据标识
	 */
	String DATA = "data";

	/**
	 * 操作成功
	 */
	String MSG_OPERATION_SUCCESS = "操作成功！";

	String getMessage();
	String getCode();
}
