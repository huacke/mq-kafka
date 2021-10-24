package com.mq.common.exception.code;


/**
 * 错误码
 */
public enum ErrorCode implements ErrorConst {

	SYSTEM_ERROR(SYSTEM_ERROR_CODE,"系统异常！"),
	/*业务异常*/
	OPERATION_ERROR(SYSTEM_ERROR_CODE,"操作失败!"),
	OPERATION_UPDATE_STATUS_ERROR(SYSTEM_ERROR_CODE, "更新业务状态失败"),


	DB_CREATE_ERROR(DB_CREATE_ERROR_CODE,"数据库新增异常"),
	DB_DELETE_ERROR(DB_DELETE_ERROR_CODE,"数据库删除异常"),
	DB_UPDATE_ERROR(DB_UPDATE_ERROR_CODE,"数据库更新异常"),
	DB_QUERY_ERROR(DB_QUERY_ERROR_CODE,"数据库查询异常"),
	DB_RESOURCE_NULL_ERROR(DB_RESOURCE_NULL_ERROR_CODE,"数据库中没有该资源"),

	DATA_EMPTY_ERROR(DATA_EMPTY_ERROR_CODE, "加载数据为空，请重试！"),
	DATA_DUPLICATE_ERROR(DATA_DUPLICATE_ERROR_CODE, "有重复数据存在"),
	DATA_OPER_ERROR(DATA_OPER_ERROR_CODE, "数据操作异常"),
	DATA_NOT_EXIST_ERROR(DATA_NOT_EXIST_ERROR_CODE, "数据不存在"),
	DATA_INIT_ERROR(DATA_INIT_ERROR_CODE,"数据加载失败，请重试！"),
	DATA_INIT_FORM_ERROR(DATA_INIT_FORM_ERROR_CODE,"初始化表单数据失败，请重试！"),
	DATA_DELETE_LINKED_ERROR(DATA_DELETE_LINKED_ERROR_CODE,"删除失败，存在关联数据！"),

	REQ_METHOD_TYPE_ERROR(REQ_METHOD_TYPE_ERROR_CODE, "请求方法类型有误"),
	REQ_SESSION_TIMEOUT_ERROR(REQ_SESSION_TIMEOUT_ERROR_CODE, "请求会话超时"),
	REQ_SERVER_ERROR(REQ_SERVER_ERROR_CODE, "请求服务器异常"),
	REQ_SERVER_BUSY_ERROR(REQ_SERVER_BUSY_ERROR_CODE,"服务繁忙，请求太频繁"),
	REQ_VALIDATE_ERROR(REQ_VALIDATE_ERROR_CODE,"请求校验有误"),

	/*参数错误*/
	REQ_PARA_NORULE_ERROR(REQ_PARA_NORULE_ERROR_CODE, "参数格式非法错误"),
	REQ_PARA_TYPE_NOMATCH_ERROR(REQ_PARA_TYPE_NOMATCH_ERROR_CODE,"参数类型不匹配"),
	REQ_PARA_INCOMPLETE_ERROR(REQ_PARA_INCOMPLETE_ERROR_CODE,"必传参数缺失"),

	/*消息队列相关*/
	MQ_PARAM_ERROR(MQ_PARAM_ERROR_CODE,"MQ消息参数错误"),
	MQ_SEND_ERROR(MQ_SEND_ERROR_CODE,"MQ发送消息出错"),
	MQ_SEND_DUPLICATE_ERROR(MQ_SEND_DUPLICATE_ERROR_CODE,"MQ消息重复发送"),
	MQ_SEND_IGNORE_ERROR(MQ_SEND_IGNORE_ERROR_CODE,"MQ消息重复发送"),
	SUCCESS("0","Success"),
	ERROR("1","Error");
	private String code;
	private String message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess(){
		return this.code.equals("0");
	}
}
