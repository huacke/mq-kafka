package com.mq.common.exception;

import com.alibaba.fastjson.JSONException;
import com.mq.common.response.ResponseResult;
import com.mq.common.exception.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Set;

/**
 * 异常处理器
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handle(ValidationException exception) {
		if(exception instanceof ConstraintViolationException){
			ConstraintViolationException exs = (ConstraintViolationException) exception;
			Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
			for (ConstraintViolation<?> item : violations) {
				log.error(item.getMessage());
				System.out.println(item.getMessage());
			}
		}
		return "bad request, " ;
	}
	@ResponseStatus(value = HttpStatus.OK)
	@ExceptionHandler({Throwable.class,Exception.class,RuntimeException.class})
	public ResponseResult handleException(Exception ex) {
		ex.printStackTrace();
		if (ex instanceof HttpMessageConversionException) {
			log.error("throw Exception!cause:{}", ex.getStackTrace());
			return ResponseResult.error(ErrorCode.REQ_PARA_NORULE_ERROR,ex);
		} else if (ex instanceof HttpMediaTypeException ||ex instanceof HttpMessageNotReadableException
				|| ex instanceof  JSONException || ex instanceof IllegalArgumentException) {
			log.error("throw Exception!cause:{}", ex.getStackTrace());
			return ResponseResult.error(ErrorCode.REQ_PARA_NORULE_ERROR,ex);
		}  else if (ex instanceof TypeMismatchException) {
			log.error("throw Exception!cause:{}", ex.getStackTrace());
			return ResponseResult.error(ErrorCode.REQ_PARA_TYPE_NOMATCH_ERROR,ex);
		} else if (ex instanceof MissingServletRequestParameterException) {
			log.error("throw Exception!cause:{}", ex.getStackTrace());
			return ResponseResult.error(ErrorCode.REQ_PARA_INCOMPLETE_ERROR,ex);
		}else if (ex instanceof BindException) {
			log.error("throw BindException!cause:{}", ex.getStackTrace());
			BindingResult result = ((BindException) ex).getBindingResult();
			List<FieldError> fes = result.getFieldErrors();
			String checkMsg = fes.get(0).getDefaultMessage();
			return  ResponseResult.error(ErrorCode.REQ_VALIDATE_ERROR,checkMsg);
		} else if (ex instanceof MethodArgumentNotValidException) {
			log.error("throw MethodArgumentNotValidException!cause:{}", ex.getStackTrace());
			BindingResult result = ((MethodArgumentNotValidException) ex).getBindingResult();
			List<FieldError> fes = result.getFieldErrors();
			String checkMsg = fes.get(0).getDefaultMessage();
			return ResponseResult.error(ErrorCode.REQ_VALIDATE_ERROR, checkMsg);
		} else if (ex instanceof DuplicateKeyException) {
			log.error("throw DuplicateKeyException!cause:{}", ex);
			return ResponseResult.error(ErrorCode.DATA_DUPLICATE_ERROR,ex);
		}else if (ex instanceof BussinessException) {
			log.error("throw WaltonException!cause:{}", ex.getStackTrace());
			BussinessException error = ((BussinessException) ex);
			return ResponseResult.error(error);
		} else if (ex.getCause() instanceof BussinessException) {
			log.error("throw WaltonException!cause:{}", ex.getStackTrace());
			BussinessException error = ((BussinessException) ex.getCause());
			return ResponseResult.error(error);
		}
		return ResponseResult.error(ex);
	}
}
