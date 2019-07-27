package com.mq.common.config.reids;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis配置类
 */
@Slf4j
@Configuration
@ConditionalOnClass({RedisOperations.class, RedisPropConfig.class,RedisCustomPropConfig.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {

    @Autowired
    private RedisPropConfig redisPropConfig;
    @Autowired
    private RedisCustomPropConfig redisCustomPropConfig;


    @Bean(value = "redisConnectionFactory")
    public JedisConnectionFactory redisConnectionFactory() throws Exception {
        JedisConnectionFactory cacheObject;
        if(redisCustomPropConfig.isUseCluster()){
            cacheObject = new JedisConnectionFactory(redisClusterConfiguration(),jedisPoolConfig());
        }else if (redisCustomPropConfig.isUseSentinel()) {
            cacheObject = new JedisConnectionFactory(redisSentinelConfiguration(),jedisPoolConfig());
        } else {
            cacheObject = new JedisConnectionFactory(redisStandaloneConfiguration());
            cacheObject.setUsePool(true);
            cacheObject.setPoolConfig(jedisPoolConfig());
        }
        cacheObject.afterPropertiesSet();
        return cacheObject;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(redisPropConfig.getMaxIdle());
        config.setMinIdle(redisPropConfig.getMinIdle());
        config.setMaxTotal(redisPropConfig.getMaxActive());
        config.setMaxWaitMillis(redisPropConfig.getMaxWaitMillis());
        config.setTimeBetweenEvictionRunsMillis(redisPropConfig.getTimeBetweenEvictionRuns());
        return config;
    }

    @Bean(value = "redisStandaloneConfiguration")
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisPropConfig.getHost());
        configuration.setPort(redisPropConfig.getPort());
        configuration.setPassword(redisPropConfig.getPassword());
        configuration.setDatabase(redisPropConfig.getDatabase());
        return configuration;
    }
    @Bean(value = "redisSentinelConfiguration")
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.setSentinels(redisNodes());
        configuration.setMaster(redisPropConfig.getMaster());
        configuration.setPassword(redisPropConfig.getPassword());
        configuration.setDatabase(redisPropConfig.getDatabase());
        return configuration;
    }
    @Bean(value = "redisClusterConfiguration")
    public RedisClusterConfiguration redisClusterConfiguration() {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration();
        clusterConfiguration.setClusterNodes(redisNodes());
        clusterConfiguration.setPassword(redisPropConfig.getPassword());
        return clusterConfiguration;
    }

    @Bean(value = "redisNodes")
    public RedisNodeAutoConfig redisNodes() {
        RedisNodeAutoConfig config = new RedisNodeAutoConfig();
        if(redisCustomPropConfig.isUseCluster()){
            config.setSentinels(redisPropConfig.getClusterNodes());
        }else if(redisCustomPropConfig.isUseSentinel()){
            config.setSentinels(redisPropConfig.getSentinelNodes());
        }
        return config;
    }

    @Bean(name = "redisTemplate")
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        StringRedisSerializer strRedisSerializer =  new StringRedisSerializer();
        template.setKeySerializer(strRedisSerializer);
        template.setHashKeySerializer(strRedisSerializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}