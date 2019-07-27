package com.mq.kafka.util;

import com.mq.common.response.ResultHandleT;
import com.mq.utils.ExceptionFormatUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @version v1.0
 * @ClassName MsgHandleUtil
 * @Description kafka消息处理工具类
 */
public class MsgHandleUtil {
    /**
     * @Description 检查异常，依此判定消息是否发成功
     **/
    public static boolean checkSendMessageFail(Object result, Throwable e) {

        ResultHandleT resultHandleT;
        boolean failed = false;
        Throwable error = null;
        if (result != null) {
            if (result instanceof ResultHandleT) {
                resultHandleT = (ResultHandleT) result;
                error = resultHandleT.getE();
            }
        } else {
            error = e;
        }
        if (error != null) {
            Throwable cause = error.getCause();
            Class<? extends Throwable> eClass = error.getClass();
            Class<? extends Throwable> causeClass =null;
            String causeClassName="";
            if (cause != null) {
                causeClass = cause.getClass();
                causeClassName = causeClass.getSimpleName().toLowerCase();
            }
            String eClassName  = eClass.getSimpleName().toLowerCase();
            String emsg = ExceptionFormatUtil.getTrace(error,5);
            String causeMsg = ExceptionFormatUtil.getTrace(error,5);
            if(null==emsg){
                emsg="";
            }
            if(null==causeMsg){
                causeMsg="";
            }
            emsg =emsg.toLowerCase();
            causeMsg=causeMsg.toLowerCase();
            List<String> keyWords = new ArrayList<>();
            keyWords.add("load balancer does not have available server for client");
            keyWords.add("Connection refused");
            keyWords.add("Hystrix circuit short-circuited and is OPEN");
            for (String keyword : keyWords) {
                  keyword =keyword.toLowerCase();
                if (eClassName.contains(keyword) || causeClassName.contains(keyword)||emsg.contains(keyword)||causeMsg.contains(keyword)) {
                    failed = true;
                    return failed;
                }
            }
        }
        return failed;
    }
}
