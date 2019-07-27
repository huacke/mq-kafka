package com.mq.common.config.mongo;

import com.mq.common.utils.SpringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
@ConditionalOnBean(MongoClinetConfig.class)
public class MongoInitializeConfig {
    @Bean
    public MappingMongoConverter mappingMongoConverter( BeanFactory beanFactory) {
        MongoMappingContext context = null;
        MongoDbFactory dbFactory = null;
        try {
            dbFactory = SpringUtils.getBean(MongoDbFactory.class);
            context = SpringUtils.getBean(MongoMappingContext.class);
        } catch (Exception e) {
            return null;
        }
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(dbFactory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        try {
            mappingConverter.setCustomConversions(beanFactory.getBean(CustomConversions.class));
        } catch (NoSuchBeanDefinitionException ignore) {
        }
        // 不保存_class字段
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return mappingConverter;
    }
}
