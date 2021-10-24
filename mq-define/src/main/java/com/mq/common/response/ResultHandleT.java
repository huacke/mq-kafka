package com.mq.common.response;

import com.mq.common.response.code.ResponseConst;
import com.mq.common.utils.ErorFormatUtil;
import com.mq.common.utils.MathUtils;
import com.mq.common.utils.StringUtils;
import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;


@Setter
@Getter
public class ResultHandleT<T> extends BaseResponse<T> {

	private static final long serialVersionUID = -4755348328409765998L;

	public ResultHandleT() {
		super();
	}

	/**
	 * 返回成功结果
	 *
	 * @return
	 */
	public static ResultHandleT ok() {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(ResponseConst.SUCCESS_CODE, ResponseConst.MSG_OPERATION_SUCCESS);
		return resultHandleT;
	}


	public static ResultHandleT ok(int count, boolean checkCount) {
		String msg = removeFailed(count, count);
		if (checkCount) {
			if (!MathUtils.isIntThanZero(count)) {
				return ResultHandleT.error(msg);
			}
		}
		return ResultHandleT.ok(msg);
	}

	public static ResultHandleT ok(int total, int count) {
		String msg = removeFailed(total, count);
		if (!MathUtils.isIntThanZero(count)) {
			return ResultHandleT.error(msg);
		}
		return ResultHandleT.ok(msg);
	}

	public static ResultHandleT ok(Object data) {
		ResultHandleT ok = ResultHandleT.ok();
		ok.setData(data);
		return ok;
	}

	public static ResultHandleT ok(Object data, boolean checkObj) {
		if (checkObj) {
			if (ObjectUtils.isEmpty(data)) {
				return empty();
			}
		}
		return ResultHandleT.ok(data);
	}


	public static ResultHandleT ok(boolean flag) {
		if (flag) {
			return ResultHandleT.ok();
		} else {
			return ResultHandleT.error();
		}
	}

	public static ResultHandleT ok(Object[] total, int count) {
		if (total.length == count) {
			return ResultHandleT.ok();
		} else {
			if (MathUtils.isIntThanZero(count)) {
				return error(removeFailed(total.length, count));
			} else {
				return ResultHandleT.error();
			}
		}
	}
	/**
	 * 返回空查询结果
	 *
	 * @return
	 */
	public static ResultHandleT empty() {
		return ResultHandleT.error(ErrorCode.DATA_EMPTY_ERROR);
	}

	public static ResultHandleT empty(String message) {
		if (StringUtils.checkEmpty(message)) {
			return empty();
		} else {
			ResultHandleT resultHandleT = new ResultHandleT();
			resultHandleT.setResultCodeAndDesc(ErrorCode.DATA_EMPTY_ERROR_CODE, message);
			return resultHandleT;
		}
	}

	/**
	 * 返回错误结果
	 *
	 * @return
	 */
	public static ResultHandleT error() {
		return ResultHandleT.error(ErrorCode.SYSTEM_ERROR);
	}

	public static ResultHandleT error(String message) {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(ErrorCode.SYSTEM_ERROR_CODE, message);
		return resultHandleT;
	}

	public static ResultHandleT error(Throwable e) {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(ErrorCode.SYSTEM_ERROR_CODE, e.getMessage());
		resultHandleT.setErrorMsg(ErorFormatUtil.getTrace(e,18));
		resultHandleT.setE(e);
		return resultHandleT;
	}

	public static ResultHandleT error(BussinessException e) {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(e.getCode(), e.getMessage());
		resultHandleT.setErrorMsg(ErorFormatUtil.getTrace(e,18));
		resultHandleT.setData(e.getData());
		resultHandleT.setE(e);
		return resultHandleT;
	}
	public static ResultHandleT error(ErrorCode result) {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(result.getCode(), result.getMessage());
		return resultHandleT;
	}

	public static ResultHandleT error(ErrorCode code, String msg) {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(code.getCode(), code.getMessage() + "[" + msg + "]");
		return resultHandleT;
	}

	public static ResultHandleT error(ErrorCode code, Throwable e) {
		ResultHandleT resultHandleT = new ResultHandleT();
		resultHandleT.setResultCodeAndDesc(code.getCode(), code.getMessage() + "[" + e.getMessage() + "]");
		return resultHandleT;
	}

	/**
	 * 删除数据项不是全部所选
	 *
	 * @param total
	 * @param process
	 * @return
	 */
	public static String removeFailed(int total, int process) {
		return "本次共处理：" + String.valueOf(total) + "条，成功：" + String.valueOf(process) + "条！";
	}

}
