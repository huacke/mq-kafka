package com.mq.common.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类字段反射工具类
 */
public class FiledUtils {

    /**
     * 缓存的Filed映射
     */
    private final static Map<Class, Map<String, Field>> cacheFieldMap = new ConcurrentHashMap<>();


    /**
     * 对象字段转换成Map(可以获取到继承父类的字段属性)
     *
     * @param obj 对象实例
     * @return
     */
    public static Map<String, Object> transfer2Map(Object obj) {
        return transfer2Map(obj, new LinkedHashMap<String, Object>(), true);
    }

    /**
     * 对象字段转换成Map(可以获取到继承父类的字段属性)
     *
     * @param obj    对象实例
     * @param params 生成属性的map
     * @return
     */
    public static Map<String, Object> transfer2Map(Object obj, Map<String, Object> params) {
        return transfer2Map(obj, new LinkedHashMap<String, Object>(), true);
    }

    /**
     * 对象字段转换成Map(可以获取到继承父类的字段属性)
     *
     * @param obj     对象实例
     * @param params  生成属性的map
     * @param isSuper 是否获取父类属性
     * @return
     */
    public static Map<String, Object> transfer2Map(Object obj, Map<String, Object> params, boolean isSuper) {
        if (null == obj) {
            return null;
        }
        if (null == params) {
            params = new LinkedHashMap<String, Object>();
        }
        Class<?> declaredClass = obj.getClass();
        PropertyDescriptor[] declaredFields = getAllPropertyDescriptors(declaredClass, isSuper);
        try {
            for (int i = 0; i < declaredFields.length; i++) {
                PropertyDescriptor fd = declaredFields[i];
                String fieldsName = fd.getName();
                Object val = fd.getReadMethod().invoke(obj);
                if (val != null) {
                    if (!isExcludeField(fd, obj)) {
                        params.put(fieldsName, val);
                    }
                }
            }
        } catch (Exception e) {
        }
        return params;
    }

    /**
     * 获取class的字段所有的元属性（一直向上追踪到到继承父类的所有字段属性）
     *
     * @param clazz
     * @return
     */
    public static PropertyDescriptor[] getAllPropertyDescriptors(Class<?> clazz) {
        return getAllPropertyDescriptors(clazz, true);
    }

    /**
     * 获取class的字段所有的元属性（可一直向上追踪到到继承父类的所有字段属性）
     *
     * @param clazz
     * @param isSuper 是否获取父类属性
     * @return
     */
    public static PropertyDescriptor[] getAllPropertyDescriptors(Class<?> clazz, boolean isSuper) {
        List<PropertyDescriptor> fieldList = new ArrayList<PropertyDescriptor>();
        PropertyDescriptor[] dFields = BeanUtils.getPropertyDescriptors(clazz);
        if (null != dFields && dFields.length > 0) {
            fieldList.addAll(Arrays.asList(dFields));
        }
        if (isSuper) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != Object.class) {
                PropertyDescriptor[] superFields = getAllPropertyDescriptors(superClass, isSuper);
                if (null != superFields && superFields.length > 0) {
                    for (PropertyDescriptor field : superFields) {
                        if (!hasConstain(fieldList, field)) {
                            fieldList.add(field);
                        }
                    }
                }
            }
        }
        return fieldList.toArray(new PropertyDescriptor[fieldList.size()]);
    }


    /**
     * 判断是否字段重复
     *
     * @param fieldList
     * @param field
     * @return
     */
    private static boolean hasConstain(List<PropertyDescriptor> fieldList, PropertyDescriptor field) {
        for (PropertyDescriptor fd : fieldList) {
            if (fd.getName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否字段重复
     *
     * @param fieldList
     * @param field
     * @return
     */
    private static boolean isConstain(List<Field> fieldList, Field field) {
        for (Field fd : fieldList) {
            if (fd.getName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是排除的字段
     *
     * @return
     */
    private static boolean isExcludeField(PropertyDescriptor field, Object obj) {
        Class<?> propertyType = field.getPropertyType();
        String name = field.getName();
        if ("class".equals(name) && (propertyType.equals(Class.class))) {
            return true;
        }
        return false;
    }

    /**
     * 获取field(缓存)
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getClasszField(Class clazz, String fieldName) {
        Map<String, Field> classzAllFieldsMap = getClasszAllFieldsMap(clazz);
        return classzAllFieldsMap.get(fieldName);
    }

    /**
     * 获取class的字段所有的元属性（可一直向上追踪到到继承父类的所有字段属性）
     *
     * @param clazz
     * @return
     */
    public static Field[] getClasszAllFields(Class<?> clazz) {
        Collection<Field> fields = getClasszAllFieldsMap(clazz).values();
        return fields.toArray(new Field[fields.size()]);
    }


    /**
     * 获取class的字段所有的元属性map映射（一直向上追踪到到继承父类的所有字段属性）
     *
     * @param clazz
     * @return
     */
    public static Map<String, Field> getClasszAllFieldsMap(Class<?> clazz) {

        String clazzName = clazz.getName();
        Map<String, Field> claszzFieldMap = null;
        List<Field> fieldList = new ArrayList<Field>();
        synchronized (clazzName) {
            claszzFieldMap = cacheFieldMap.get(clazz);
            if (!ObjectUtils.isEmpty(claszzFieldMap)) {
                return claszzFieldMap;
            }
            if (!cacheFieldMap.containsKey(clazz)) {
                claszzFieldMap = cacheFieldMap.get(clazz);
                if (null == claszzFieldMap) {
                    claszzFieldMap = new ConcurrentHashMap<>();
                    cacheFieldMap.putIfAbsent(clazz, claszzFieldMap);
                }
            }
            Field[] dFields = clazz.getDeclaredFields();
            if (null != dFields && dFields.length > 0) {
                fieldList.addAll(Arrays.asList(dFields));
            }
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != Object.class) {
                Map<String, Field> superFieldsMap = getClasszAllFieldsMap(superClass);
                if (!ObjectUtils.isEmpty(superFieldsMap)) {
                    Collection<Field> fds = superFieldsMap.values();
                    for (Field field : fds) {
                        if (!isConstain(fieldList, field)) {
                            fieldList.add(field);
                        }
                    }
                }
            }
            for (Field field : fieldList) {
                claszzFieldMap.putIfAbsent(field.getName(), field);
            }
        }
        return claszzFieldMap;
    }
}
