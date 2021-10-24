package com.mq.datasources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.sql.SQLException;
/**
 * 配置多数据源
 */
@Configuration
@EnableTransactionManagement
public class DynamicDataSourceConfig {

    @Autowired
    private DataSourceBuilder dataSourceBuilder;

    @Bean
    @Primary
    public DynamicDataSource dataSource() throws SQLException {
       return dataSourceBuilder.build();
    }

}
