package com.mq.datasources.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DataSourceOptionProperties {

    private Integer initialSize = 10;
    private Integer maxActive = 100;
    private Integer minIdle = 10;
    private Integer maxWait = 60000;
    private Boolean poolPreparedStatements = true;
    private Integer maxPoolPreparedStatementPerConnectionSize = 20;
    private Long timeBetweenEvictionRunsMillis = 60000L;
    private Long minEvictableIdleTimeMillis = 300000L;
    private String validationQuery = "SELECT 1";
    private Boolean testWhileIdle = true;
    private Boolean testOnBorrow = false;
    private Boolean testOnReturn = false;
    private String filters = "stat,wall,slf4j";
    @Value("${spring.datasource.connectionProperties:druid.stat.mergeSql=true;druid.stat.slowSqlMillis=3000;druid.stat.logSlowSql=true}")
    private String connectionProperties;
    @Value("${spring.datasource.useGlobalDataSourceStat:true}")
    private boolean useGlobalDataSourceStat = true;
    @Value("${spring.datasource.druid.stat-view-servlet.enabled:true}")
    private Boolean statViewServletEnabled = true;
    @Value("${spring.datasource.druid.stat-view-servlet.url-pattern:/druid/*}")
    private String statViewServletUrlPattern = "/druid/*";


}
