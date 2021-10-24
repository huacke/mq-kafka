package com.data.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 * 
 */
public class ResponseResult extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	
	public ResponseResult() {
		put("code", 0);
	}
	
	public static ResponseResult error() {
		return error(500, "未知异常，请联系管理员");
	}
	public static ResponseResult error(GenException error) {
		return error(error.getCode(), error.getMsg());
	}
	public static ResponseResult error(String msg) {
		return error(500, msg);
	}
	
	public static ResponseResult error(int code, String msg) {
		ResponseResult r = new ResponseResult();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static ResponseResult ok(String msg) {
		ResponseResult r = new ResponseResult();
		r.put("msg", msg);
		return r;
	}
	
	public static ResponseResult ok(Map<String, Object> map) {
		ResponseResult r = new ResponseResult();
		r.putAll(map);
		return r;
	}
	
	public static ResponseResult ok() {
		return new ResponseResult();
	}

	public ResponseResult put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
