package com.mq.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public  class ExceptionFormatUtil {

    private static final Log log = LogFactory.getLog(ExceptionFormatUtil.class);

    private static String filterPackage = "com.mq";

    /**
     * 将Exception的异常栈按简化输出(只输出 filterPackage路径开头的类调用栈)
     * <br/>
     * 异常名 Caused by:(异常出处类名.方法名:行号<-..)
     *
     * @param e
     * @return
     */
    public static String getTrace(Throwable e) {
        return getTrace(e, true,null);
    }


    public static String getTrace(Throwable e,Integer level) {
        return getTrace(e, true,level);
    }


    public static String getTrace(Throwable e, boolean filterPackage,Integer level) {
        if (e == null) {
            return "";
        }
        try {
            return format(e, filterPackage,level);
        } catch (Exception e1) {
            //如果格式化不出来，直接原文输出
            log.error("格式化异常出错：" + e.getMessage());
            log.error("格式化异常出错：原文是：", e);
        }
        return "";
    }

    private static String format(Throwable e, boolean filterPackage,Integer level) {
        StringBuilder exceptionResult = new StringBuilder(e.toString());
        getFormatString(e, filterPackage, exceptionResult,level,0);
        return exceptionResult.toString();
    }


    private static void getFormatString(Throwable e, boolean filterPackage, StringBuilder exceptionResult,Integer level,int count) {
        StackTraceElement[] stList=null;
        Throwable cause = e.getCause();
        if(cause!=null){
            stList   = cause.getStackTrace();
        }else{
            stList = e.getStackTrace();
        }
        if (stList == null) {
            return;
        }
        if (exceptionResult.length() > 0) {
            exceptionResult.append(" Caused by:(");
            if(cause!=null){
                String causes = cause.toString();
                exceptionResult.append(causes);
                exceptionResult.append("<- ");
            }
        }
        for (StackTraceElement ste : stList) {
            if (filterPackage) {
                if (!ste.getClassName().contains(ExceptionFormatUtil.filterPackage)) {
                    continue;
                }
            }
            if(level!=null && count >= level ){
                return;
            }
            String className = ste.getClassName();
            String fileName = ste.getFileName();
            String methodName = ste.getMethodName();
            int lineNumber = ste.getLineNumber();
            if(null == fileName){
                fileName = className;
            }
            if(lineNumber<0){
                continue;
            }
            fileName = fileName.replace(".java", ".");
            fileName = fileName.replace(".groovy", ".");
            exceptionResult.append(fileName);
            exceptionResult.append("   ");
            exceptionResult.append(methodName);
            exceptionResult.append(":");
            exceptionResult.append(lineNumber);
            exceptionResult.append(" <- ");
            if(level!=null){
                count ++;
            }
        }
        if (exceptionResult.lastIndexOf(" <- ") == exceptionResult.length() - 4) {
            exceptionResult.delete(exceptionResult.length() - 4, exceptionResult.length());
        }
        exceptionResult.append(")");
        Throwable e1 = e.getCause();
        if (e1 != null) {
            getFormatString(e1, filterPackage, exceptionResult,4,count);
        }
    }

    public static void main(String[] a) {
        try {
            int i = 5 / 0;
        } catch (Exception e) {
            System.out.println(getTrace(e));
        }


    }

}
