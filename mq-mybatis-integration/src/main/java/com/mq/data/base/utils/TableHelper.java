package com.mq.data.base.utils;

import com.mq.common.utils.FiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import javax.persistence.Id;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * 表元数据工具类
 */
@Slf4j
public class TableHelper {

    public static <T> T getTablePrimaryKey(Object entryBean) throws Exception {

        PropertyDescriptor tablePrimaryKeyField = findTablePrimaryKeyField(entryBean);
        if (null == tablePrimaryKeyField) {
            throw new RuntimeException("not Found primaryKeyField!");
        }
        //获取主键字段的值
        return (T) tablePrimaryKeyField.getReadMethod().invoke(entryBean);

    }

    /**
     * 查找表定义的主键元信息
     *
     * @param entryBean
     * @return
     */
    private static PropertyDescriptor findTablePrimaryKeyField(Object entryBean) {
        Class<?> declaringClass = entryBean.getClass();
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(declaringClass);
        for (PropertyDescriptor pd : propertyDescriptors) {
            String fieldName = pd.getName();
            try {
                Field field = FiledUtils.getClasszField(declaringClass, fieldName);
                if (field != null) {
                    Id idAnnotation = field.getAnnotation(Id.class);
                    if (idAnnotation != null) {
                        return pd;
                    }
                }
            } catch (Exception e) {
            }
        }
        return null;
    }


}
