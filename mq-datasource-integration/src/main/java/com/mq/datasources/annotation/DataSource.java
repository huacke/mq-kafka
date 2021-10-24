package com.mq.datasources.annotation;

import com.mq.datasources.DataSourceNames;

import java.lang.annotation.*;

/**
 * 多数据源注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    DataSourceNames value() default DataSourceNames.MASTER;
}
