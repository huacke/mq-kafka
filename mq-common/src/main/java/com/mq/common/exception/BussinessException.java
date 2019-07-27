package com.mq.common.exception;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.mq.common.exception.code.ErrorCode;

/**
 * 自定义异常
 */
public class BussinessException extends HystrixBadRequestException {
	/**
	 * 序列化id
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 错误响应消息
	 */
    private String message;
	/**
	 * 错误响应消息码
	 */
	private String code = ErrorCode.SYSTEM_ERROR_CODE;

	public BussinessException(Throwable e) {
		super(e.getMessage(),e);
		if(e instanceof BussinessException){
			BussinessException error = ((BussinessException)e);
			this.code = error.getCode()==null? ErrorCode.SYSTEM_ERROR_CODE: error.getCode();
			this.message =error.getMessage();
		}else if(e.getCause() instanceof BussinessException)	{
			BussinessException error = ((BussinessException)e.getCause());
			this.code = error.getCode()==null? ErrorCode.SYSTEM_ERROR_CODE: error.getCode();
			this.message = error.getMessage();
		}
	}
	public BussinessException(Exception e) {
		super(e.getMessage(),e);
		if(e instanceof BussinessException){
			BussinessException error = ((BussinessException)e);
			this.code = error.getCode()==null? ErrorCode.SYSTEM_ERROR_CODE: error.getCode();
			this.message =error.getMessage();
		}else if(e.getCause() instanceof BussinessException)	{
			BussinessException error = ((BussinessException)e.getCause());
			this.code = error.getCode()==null? ErrorCode.SYSTEM_ERROR_CODE: error.getCode();
			this.message = error.getMessage();
		}
	}
	public BussinessException(BussinessException e) {
		super(e.getMessage());
		this.code = e.getCode();
		this.message = e.getMessage();
	}
	public BussinessException(String code,String message) {
		super(message);
		this.code =code;
		this.message = message;
	}
    public BussinessException(String message) {
		super(message);
		this.message = message;
	}
	public BussinessException(String message, Throwable e) {
		super(message, e);
		this.message = message + "[" + e.getMessage() + "]";
	}
	public BussinessException(ErrorCode code) {
		super(code.getMessage());
		this.code = code.getCode();
		this.message = code.getMessage();
	}
	public BussinessException(ErrorCode code, String msg) {
		super(code.getMessage() + "[" + msg + "]");
		this.code = code.getCode();
		this.message = code.getMessage() + "[" + msg + "]";
	}
	public BussinessException(ErrorCode code, Throwable e) {
		super(code.getMessage());
		this.code = code.getCode();
		this.message = code.getMessage() + "[" + e.getMessage() + "]";
	}
	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
