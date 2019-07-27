package com.mq.common.response;

import com.mq.common.response.code.ResponseConst;
import com.mq.common.utils.MathUtils;
import com.mq.common.utils.StringUtils;
import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import org.springframework.util.ObjectUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * 格式化响应信息的工具类
 * 
 * @Author 
 * @Create 
 * @modify
 */
@SuppressWarnings("rawtypes")
public class ResponseResult<T>  extends HashMap<String, Object> implements Serializable{
	@Override
	public ResponseResult put(String key, Object value) {
		super.put(key, value);
		return this;
	}
	/**
	 * 返回成功结果
	 * @return
	 */
	public static ResponseResult ok() {
		ResponseResult resultMap = new ResponseResult();
		resultMap.put(ResponseConst.CODE, ResponseConst.SUCCESS_CODE);
		resultMap.put(ResponseConst.MESSAGE,ResponseConst.MSG_OPERATION_SUCCESS);
		return resultMap;
	}
	public static ResponseResult ok(int count,boolean checkCount) {
		String msg = removeFailed(count, count);
		if(checkCount){
			if(!MathUtils.isIntThanZero(count)){
				return ResponseResult.error(msg);
			}
		}
		return ResponseResult.ok(msg);
	}
	public static ResponseResult ok(int total,int count) {
		String msg = removeFailed(total, count);
		if(!MathUtils.isIntThanZero(count)){
			return ResponseResult.error(msg);
		}
		return ResponseResult.ok(msg);
	}
	public static ResponseResult ok(Object data) {
		return ResponseResult.ok().put(ResponseConst.DATA, data);
	}
	public static ResponseResult ok(Object data,boolean checkObj) {
		if(checkObj){
			if(ObjectUtils.isEmpty(data)){
				return empty();
			}
		}
		return ResponseResult.ok(data);
	}
	public static ResponseResult ok(Object data,String title) {
		Map<String, Object> objMap = new HashMap<String, Object>();
		objMap.put(title, data);
		return ResponseResult.ok().put(ResponseConst.DATA, objMap);
	}

	public static ResponseResult ok(Object data,String title,boolean isCheckObj) {
		if(isCheckObj) {
			if (ObjectUtils.isEmpty(data)) {
				return empty();
			}
		}
		return ResponseResult.ok(data,title);
	}
	public static ResponseResult ok(boolean flag) {
		if(flag){
			return  ResponseResult.ok();
		}else{
			return  ResponseResult.error();
		}
	}
	public static ResponseResult ok(Object[] total, int count) {
		if(total.length == count){
			return ResponseResult.ok();
		}else{
			if(MathUtils.isIntThanZero(count)){
				return error(removeFailed(total.length, count));
			}else{
				return ResponseResult.error();
			}
		}
	}
	/**
	 * 返回空查询结果
	 * @return
	 */
	public static ResponseResult empty(){
		return ResponseResult.error(ErrorCode.DATA_EMPTY_ERROR);
	}
	public static ResponseResult empty(String message) {
		if(StringUtils.checkEmpty(message)) {
			return empty();
		} else {
			ResponseResult resultMap = new ResponseResult();
			resultMap.put(ResponseConst.CODE, ErrorCode.DATA_EMPTY_ERROR_CODE);
			resultMap.put(ResponseConst.MESSAGE, message);
			return resultMap;
		}
	}
	/**
	 * 返回错误结果
	 * @return
	 */
	public static ResponseResult error() {
		return ResponseResult.error(ErrorCode.SYSTEM_ERROR);
	}
	public static ResponseResult error(String message) {
		ResponseResult resultMap = new ResponseResult();
		resultMap.put(ResponseConst.CODE, ErrorCode.SYSTEM_ERROR_CODE);
		resultMap.put(ResponseConst.MESSAGE, message);
		return resultMap;
	}
	public static ResponseResult error(Throwable e) {
		ResponseResult resultMap = new ResponseResult();
		resultMap.put(ResponseConst.CODE,ErrorCode.SYSTEM_ERROR_CODE);
		resultMap.put(ResponseConst.MESSAGE, e.getMessage());
		return resultMap;
	}
	public static ResponseResult error(BussinessException e) {
		ResponseResult resultMap = new ResponseResult();
		resultMap.put(ResponseConst.CODE, e.getCode());
		resultMap.put(ResponseConst.MESSAGE, e.getMessage());
		return resultMap;
	}
	public static ResponseResult error(ErrorCode result) {
		ResponseResult resultMap = new ResponseResult();
		resultMap.put(ResponseConst.CODE,result.getCode());
		resultMap.put(ResponseConst.MESSAGE, result.getMessage());
		return resultMap;
	}
	public static ResponseResult error(ErrorCode code,String msg) {
		ResponseResult r = new ResponseResult();
		r.put(ResponseConst.CODE, code.getCode());
		r.put(ResponseConst.MESSAGE, code.getMessage()+"["+msg+"]");
		return r;
	}
	public static ResponseResult error(ErrorCode code,Throwable e) {
		ResponseResult r = new ResponseResult();
		r.put(ResponseConst.CODE, code.getCode());
		r.put(ResponseConst.MESSAGE, code.getMessage()+"["+e.getMessage()+"]");
		return r;
	}
	/**
	 * 删除数据项不是全部所选
	 * @param total
	 * @param process
	 * @return
	 */
	public static String removeFailed(int total, int process){
		return "本次共处理："+String.valueOf(total)+"条，成功："+String.valueOf(process)+"条！";
	}
}