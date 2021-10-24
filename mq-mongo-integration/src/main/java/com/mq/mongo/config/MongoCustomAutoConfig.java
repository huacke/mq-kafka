package com.mq.mongo.config;

import com.mongodb.Block;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.connection.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(MongoClient.class)
@EnableConfigurationProperties({MongoProperties.class, MongOptionProperties.class})
@ConditionalOnMissingBean(type = {"org.springframework.data.mongodb.MongoDatabaseFactory"})
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
public class MongoCustomAutoConfig  {

    @Bean
    @ConditionalOnMissingBean(MongoClientSettings.class)
    public MongoClientSettings mongoClientOptions(MongOptionProperties mongoOptionProperties) {
        if (mongoOptionProperties == null) {
            return MongoClientSettings.builder().build();
        }
        return MongoClientSettings.builder()
                .applyToClusterSettings(new Block<ClusterSettings.Builder>() {
                    @Override
                    public void apply(final ClusterSettings.Builder builder) {
                        builder.serverSelectionTimeout(mongoOptionProperties.getServerSelectionTimeout(), TimeUnit.MILLISECONDS);
                        builder.localThreshold(mongoOptionProperties.getLocalThreshold(), TimeUnit.MILLISECONDS);
                    }
                })

                .applyToServerSettings(new Block<ServerSettings.Builder>() {
                    @Override
                    public void apply(final ServerSettings.Builder builder) {
                        builder.heartbeatFrequency(mongoOptionProperties.getHeartbeatFrequency(), TimeUnit.MILLISECONDS);
                        builder.minHeartbeatFrequency(mongoOptionProperties.getMinHeartbeatFrequency(), TimeUnit.MILLISECONDS);
                    }
                })
                .applyToSocketSettings(new Block<SocketSettings.Builder>() {
                    @Override
                    public void apply(final SocketSettings.Builder builder) {
                        builder.readTimeout(mongoOptionProperties.getSocketTimeout(), TimeUnit.MILLISECONDS);
                        builder.connectTimeout(mongoOptionProperties.getConnectTimeout(), TimeUnit.MILLISECONDS);
                    }
                })
                .applyToConnectionPoolSettings(new Block<ConnectionPoolSettings.Builder>() {
                    @Override
                    public void apply(final ConnectionPoolSettings.Builder builder) {
                        builder.minSize(mongoOptionProperties.getMinPoolSize());
                        builder.maxSize(mongoOptionProperties.getMaxPoolSize());
                        builder.maxWaitTime(mongoOptionProperties.getMaxWaitTime(), TimeUnit.MILLISECONDS);
                        builder.maxConnectionIdleTime(mongoOptionProperties.getMaxConnectionIdleTime(), TimeUnit.MILLISECONDS);
                        builder.maxConnectionLifeTime(mongoOptionProperties.getMaxConnectionLifeTime(), TimeUnit.MILLISECONDS);
                    }
                })
                .applyToSslSettings(new Block<SslSettings.Builder>() {
                    @Override
                    public void apply(final SslSettings.Builder builder) {
                        builder.enabled(mongoOptionProperties.getSslEnabled());
                        builder.invalidHostNameAllowed(mongoOptionProperties.getSslInvalidHostNameAllowed());
                    }
                }).build();
    }


    @Bean
    @ConditionalOnMissingBean(MongoClient.class)
    public MongoClient mongo(MongoProperties properties, Environment environment,
                             ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers,
                             MongoClientSettings mongoClientSettings) {
        return new MongoClientFactory(properties, environment,
                builderCustomizers.orderedStream().collect(Collectors.toList()))
                .createMongoClient( mongoClientSettings);
    }



    @Bean
    @ConditionalOnBean(MongoClient.class)
    @ConditionalOnMissingBean(MongoConverter.class)
    public MappingMongoConverter mappingMongoConverter(BeanFactory beanFactory,MongoDatabaseFactory dbFactory,MongoMappingContext context) {
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
