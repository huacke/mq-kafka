package com.mq.web.feign;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.mq.common.response.ResponseResult;
import com.mq.common.response.ResultHandleT;
import com.mq.common.response.code.ResponseConst;
import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import com.mq.common.exception.code.ErrorConst;
import com.mq.common.utils.StringUtils;
import com.mq.utils.GsonHelper;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Aspect
@Component
@Slf4j
public class FeignClientInvokerIntercepter {

    //存放Fegin每次接口调用的生成的requestTemplate
   public static ThreadLocal<RequestTemplate> requestTemplateThreadLocal = new TransmittableThreadLocal<>();

    /* *
     * @Description 拦截FeignClient 类里面的所有方法，对异常进行统一处理
     * @param [pjp]
     * @return  java.lang.Object
     **/
    @Around("@within(org.springframework.cloud.openfeign.FeignClient)&&execution(* com.mq..*.*(..))")
    public Object doIntercepter(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName();
        Object target = pjp.getTarget();
        Object baseResponse = null;
        Object[] params = pjp.getArgs();
        ResponseResult responseResult = null;
        ResultHandleT resultHandleT = null;
        String remoteErromsg="";
        try {
            baseResponse = pjp.proceed();
            if (baseResponse instanceof ResponseResult) {
                responseResult = (ResponseResult) baseResponse;
                resultHandleT =ResultHandleT.ok();
                resultHandleT.setResultCodeAndDesc((String) responseResult.get(ResponseConst.CODE), (String) responseResult.get(ResponseConst.MESSAGE));
                resultHandleT.setData(responseResult.get(ResponseConst.DATA));
            }else if (baseResponse instanceof ResultHandleT){
                resultHandleT = (ResultHandleT)baseResponse;
            }else{
                return baseResponse;
            }
            //远程调用有exception在调用端也抛出
            if (resultHandleT != null) {
                String code = resultHandleT.getCode();
                if (StringUtils.isNotEmpty(code)) {
                    if (!ResponseConst.SUCCESS_CODE.equals(code)) {
                        remoteErromsg=resultHandleT.getErrorMsg();
                        throw new BussinessException(code, resultHandleT.getMessage());
                    }
                }
            }

            return resultHandleT;
        }catch (BussinessException e){
            log.error(String.format(" ===== invoker remote service error  %s  method  %s  params: %s cause by : %s", target, method, GsonHelper.toJson(params),remoteErromsg));
            throw e;
        } catch (Throwable e) {
            log.error(String.format(" ===== invoker remote service error  %s  method  %s  params: %s cause by: ", target, method, GsonHelper.toJson(params)),e);
            String errorCode = ErrorConst.SYSTEM_ERROR_CODE;
            String errorMsg = ErrorCode.SYSTEM_ERROR.getMessage();
            throw new BussinessException(errorCode, errorMsg,e);
        }finally {
            //释放主线程中的 requestTemplate对象资源
            requestTemplateThreadLocal.remove();
        }
    }


    //拦截fegin 接口调用
    @Bean
    public RequestInterceptor requestInterceptor() {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if(null==attributes){
                    return;
                }
                HttpServletRequest request = attributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String values = request.getHeader(name);
                        template.header(name, values);
                    }
                }
                //在上下文中设置requestTemplate实例
                //有两种情况 1：在主线程中调用（tomcat主线程） 2，基于histryx线程池调用，那时候threadlocal存放的线程池线程的数据，所以在FeignHystrixConcurrencyStrategy中做了hook
                requestTemplateThreadLocal.set(template);
            }

        };
    }

}
