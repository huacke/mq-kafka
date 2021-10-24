package com.mq.config;

import com.mq.common.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class MybatisConfig {
   /* @Bean
    SqlSessionFactory mysqlSessionFactory(DynamicDataSource dynamicDataSource) throws Exception {
        ExtSqlSessionFactoryBean extSqlSessionFactoryBean = new ExtSqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //String packageSearchPath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders("com.walton.**.dao.**.*.xml"));
        //extSqlSessionFactoryBean.setMapperLocations(resolver.getResources(packageSearchPath));
        extSqlSessionFactoryBean.setExtPackages("com.mq.**.dao.mapper.**.ext");
        extSqlSessionFactoryBean.setDataSource(dynamicDataSource);
        extSqlSessionFactoryBean.setPlugins(new Interceptor[]{pageHelper()});
        return  extSqlSessionFactoryBean.getObject();
    }*/
    /**
     * 分页插件
     */
    @Bean
    public PageHelper pageHelper(){
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum","true");
        properties.setProperty("rowBoundsWithCount","true");
        properties.setProperty("reasonable","true");
        properties.setProperty("dialect","mysql");    //配置mysql数据库的方言
        pageHelper.setProperties(properties);
        return pageHelper;
    }


}
