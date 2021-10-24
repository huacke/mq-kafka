package com.mq.datasources;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.wall.WallFilter;
import com.mq.common.utils.StringUtils;
import com.mq.datasources.config.DataSourceOptionProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DataSourceBuilder {

    private static final String DATASOUCE_PREFIX = "spring.datasource.druid.";

    @Autowired
    private Environment env;
    @Autowired
    private DataSourceOptionProperties dataSourceOptionProperties;
    @Autowired
    private WallFilter configWallFilter;


    private DataSourceBaseConfig buildConfig(String sourcename) {

        String url = env.getProperty(String.format("%s%s%s", DATASOUCE_PREFIX, sourcename, ".url"));
        String username = env.getProperty(String.format("%s%s%s", DATASOUCE_PREFIX, sourcename, ".username"));
        String password = env.getProperty(String.format("%s%s%s", DATASOUCE_PREFIX, sourcename, ".password"));
        if (StringUtils.isEmpty(url) && StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
            return null;
        }
        DataSourceBaseConfig dataSourceBaseConfig = new DataSourceBaseConfig();
        BeanUtils.copyProperties(dataSourceOptionProperties, dataSourceBaseConfig);
        dataSourceBaseConfig.setUrl(url);
        dataSourceBaseConfig.setUsername(username);
        dataSourceBaseConfig.setPassword(password);
        return dataSourceBaseConfig;
    }

    public DynamicDataSource build() throws SQLException {

        DataSourceNames[] dataSourceNames = DataSourceNames.values();
        DataSource defaultTargetDataSource = null;
        Map<Object, Object> targetDataSources = new HashMap<>();
        for (DataSourceNames sn : dataSourceNames) {
            String sourceName = sn.getCode();
            DataSourceBaseConfig sourceConfig = buildConfig(sourceName);
            if (null != sourceConfig) {
                DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
                configure(dataSource,sourceConfig);
                dataSource.setUrl(sourceConfig.getUrl());
                dataSource.setUsername(sourceConfig.getUsername());
                dataSource.setPassword(sourceConfig.getPassword());
                targetDataSources.put(sourceName, dataSource);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dataSource.init();
                        } catch (SQLException e) {
                            log.error("init dataSource error cause by :",e);
                        }
                    }
                }).start();

                if (DataSourceNames.MASTER.equals(sn)) {
                    defaultTargetDataSource = dataSource;
                }
            }
        }
        if (null == defaultTargetDataSource) {
            throw new RuntimeException("默认数据源不能为空,请检查配置是否有效！");
        }
       return new DynamicDataSource(defaultTargetDataSource,targetDataSources);
    }



    private void configure(DruidDataSource druidDataSource,DataSourceBaseConfig dataSourceBaseConfig) throws SQLException {

        if(dataSourceBaseConfig!=null && druidDataSource!=null){
            druidDataSource.setUrl(dataSourceBaseConfig.getUrl());
            druidDataSource.setUsername(dataSourceBaseConfig.getUsername());
            druidDataSource.setPassword(dataSourceBaseConfig.getPassword());
            druidDataSource.setInitialSize(dataSourceBaseConfig.getInitialSize());
            druidDataSource.setMaxActive(dataSourceBaseConfig.getMaxActive());
            druidDataSource.setMinIdle(dataSourceBaseConfig.getMinIdle());
            druidDataSource.setMaxWait(dataSourceBaseConfig.getMaxWait());
            druidDataSource.setPoolPreparedStatements(dataSourceBaseConfig.getPoolPreparedStatements());
            druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(dataSourceBaseConfig.getMaxPoolPreparedStatementPerConnectionSize());
            druidDataSource.setTimeBetweenEvictionRunsMillis(dataSourceBaseConfig.getTimeBetweenEvictionRunsMillis());
            druidDataSource.setMinEvictableIdleTimeMillis(dataSourceBaseConfig.getMinEvictableIdleTimeMillis());
            druidDataSource.setValidationQuery(dataSourceBaseConfig.getValidationQuery());
            druidDataSource.setTestWhileIdle(dataSourceBaseConfig.getTestWhileIdle());
            druidDataSource.setTestOnBorrow(dataSourceBaseConfig.getTestOnBorrow());
            druidDataSource.setTestOnReturn(dataSourceBaseConfig.getTestOnReturn());
            druidDataSource.setFilters(dataSourceBaseConfig.getFilters());

            List<Filter> oldfilters = druidDataSource.getProxyFilters();
            List<Filter> newfilters = new CopyOnWriteArrayList();
            if (!CollectionUtils.isEmpty(oldfilters)) {
                List<Filter> selectFilters = oldfilters.stream().filter((it) -> {
                    return !(it instanceof WallFilter);
                }).collect(Collectors.toList());
                oldfilters.clear();
                if (!CollectionUtils.isEmpty(selectFilters)) {
                    newfilters.addAll(selectFilters);
                }
            }
            newfilters.add(this.configWallFilter);
            druidDataSource.setProxyFilters(newfilters);
        }

    }


    @Data
    public static class DataSourceBaseConfig extends DataSourceOptionProperties {
        private String url;
        private String username;
        private String password;
    }

}
