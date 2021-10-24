package com.mq.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mq.common.entity.BaseObject;
import com.mq.common.response.code.ResponseConst;
import lombok.Data;

@Data
public class BaseResponse<T> extends BaseObject {

	private static final long serialVersionUID = -475534832840975335L;
	/**
	 * 返回编码
	 */
	String code = ResponseConst.SUCCESS_CODE;
	/**
	 * 提示信息标识
	 */
	String message = ResponseConst.MSG_OPERATION_SUCCESS;


	/**
	 * 异常堆栈信息
	 */
	private String errorMsg ="";

	// 返回数据
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	transient private Throwable e;

	public BaseResponse() {
		super();
	}

	public static <T> BaseResponse<T> newInstance() {
		return new BaseResponse<>();
	}

	public void setResultCodeAndDesc(String resultCode, String resultDesc) {
		this.code = resultCode;
		this.message = resultDesc;
	}

}
