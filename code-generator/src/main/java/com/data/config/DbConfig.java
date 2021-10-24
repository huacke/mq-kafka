package com.data.config;

import com.data.dao.GeneratorDao;
import com.data.dao.MysqlGeneratorDao;
import com.data.utils.GenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 数据库配置
 */
@Configuration
public class DbConfig {
    @Value("${generator.database: mysql}")
    private String database;
    @Autowired
    private MysqlGeneratorDao mySQLGeneratorDao;
    @Bean
    @Primary
    public GeneratorDao getGeneratorDao(){
        if("mysql".equalsIgnoreCase(database)){
            return mySQLGeneratorDao;
        }else {
            throw new GenException("不支持当前数据库：" + database);
        }
    }
}
