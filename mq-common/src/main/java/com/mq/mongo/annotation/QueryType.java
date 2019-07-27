package com.mq.mongo.annotation;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 查询类型
 * 1. equals：相等
 * 2. like:mongodb的like查询
 * 3. in:用于列表的in类型查询
 */
public enum QueryType {
    EQUALS {
        @Override
        public Criteria buildCriteria(QueryFieldType queryFieldAnnotation, Field field, Object value) {
            if (check(queryFieldAnnotation, field, value)) {
                String queryField = getQueryFieldName(queryFieldAnnotation, field);
                return Criteria.where(queryField).is(value.toString());
            }
            return new Criteria();
        }
    },
    LIKE {
        @Override
        public Criteria buildCriteria(QueryFieldType queryFieldAnnotation, Field field, Object value) {
            if (check(queryFieldAnnotation, field, value)) {
                String queryField = getQueryFieldName(queryFieldAnnotation, field);
                return Criteria.where(queryField).regex(value.toString());
            }
            return new Criteria();
        }
    },
    IN {
        @Override
        public Criteria buildCriteria(QueryFieldType queryFieldAnnotation, Field field, Object value) {
            if (check(queryFieldAnnotation, field, value)) {
                if (value instanceof List) {
                    String queryField = getQueryFieldName(queryFieldAnnotation, field);
                    return Criteria.where(queryField).in((List<?>)value);
                }
            }
            return new Criteria();
        }
    };

    private static boolean check(QueryFieldType queryField, Field field, Object value) {
        return !(queryField == null || field == null || value == null);
    }

    public abstract Criteria buildCriteria(QueryFieldType queryFieldAnnotation, Field field, Object value);


    /**
     * 获取字段名称
     */
    private static String getQueryFieldName(QueryFieldType queryField, Field field) {
        String queryFieldValue = queryField.attribute();
        if (!StringUtils.hasText(queryFieldValue)) {
            queryFieldValue = field.getName();
        }
        return queryFieldValue;
    }
}

