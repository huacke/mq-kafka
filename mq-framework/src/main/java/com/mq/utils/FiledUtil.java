package com.mq.utils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @version v1.0
 * @Description 属性字段工具类
 */
public class FiledUtil {

    public static Map<String, Object> getFiledMap(Object obj) {
        return getFiledMap(obj, new LinkedHashMap<String, Object>());
    }

    /**
     * 获取对象所定义的字段值映射表
     *
     * @param obj
     * @param params
     * @return
     */
    public static Map<String, Object> getFiledMap(Object obj, Map<String, Object> params) {
        if (null == obj) {
            return null;
        }
        if (null == params) {
            params = new LinkedHashMap<String, Object>();
        }
        Field[] declaredFields = getAllFields(obj.getClass());
        try {
            for (int i = 0; i < declaredFields.length; i++) {
                Field filed = declaredFields[i];
                filed.setAccessible(true);
                String name = filed.getName();
                Object val = filed.get(obj);
                if (val != null) {
                    params.put(name, val);
                }
            }
        } catch (Exception e) {
        }
        return params;
    }

    /**
     * 反射获取类的所有字段
     *
     * @param clazz
     * @return
     */
    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<Field>();
        Field[] dFields = clazz.getDeclaredFields();
        if (null != dFields && dFields.length > 0) {
            fieldList.addAll(Arrays.asList(dFields));
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
            Field[] superFields = getAllFields(superClass);
            if (null != superFields && superFields.length > 0) {
                for (Field field : superFields) {
                    if (!hasConstain(fieldList, field)) {
                        fieldList.add(field);
                    }
                }
            }
        }
        return fieldList.toArray(new Field[fieldList.size()]);
    }

    private static boolean hasConstain(List<Field> fieldList, Field field) {
        for (Field fd : fieldList) {
            if (fd.getName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }


    public static boolean isIgnoreClassType(Class classz) {
        Class className = classz.getClass();
        if (    className.equals(java.lang.String.class) ||
                className.equals(java.util.Date.class) ||
                className.equals(java.lang.Integer.class) ||
                className.equals(java.lang.Byte.class) ||
                className.equals(java.lang.Long.class) ||
                className.equals(java.lang.Double.class) ||
                className.equals(java.lang.Float.class) ||
                className.equals(java.lang.Character.class) ||
                className.equals(java.lang.Short.class) ||
                className.equals(java.lang.Boolean.class)) {
            return true;
        }
        return false;
    }

}