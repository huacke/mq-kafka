package com.mq.redis.config;

import com.alibaba.fastjson.JSON;
import com.mq.redis.serializer.CustomStringRedisSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;

/**
 * redis配置类
 */
@SuppressWarnings("ALL")
@Slf4j
@Configuration
@ConditionalOnClass({RedisOperations.class, RedisPropConfig.class, RedisCustomPropConfig.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {
    @Autowired
    private RedisPropConfig redisPropConfig;
    @Autowired
    private RedisCustomPropConfig redisCustomPropConfig;
    @Autowired
    private ClientResources clientResources;

    @Bean(value = "redisConnectionFactory")
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory cacheObject;
        if (redisCustomPropConfig.isUseCluster()) {
            cacheObject = new LettuceConnectionFactory(redisClusterConfiguration(), reidsClientConfig());
        } else if (redisCustomPropConfig.isUseSentinel()) {
            cacheObject = new LettuceConnectionFactory(redisSentinelConfiguration(), reidsClientConfig());
        } else {
            cacheObject = new LettuceConnectionFactory(redisStandaloneConfiguration(), reidsClientConfig());
        }
        return cacheObject;
    }

    @Bean
    public LettuceClientConfiguration reidsClientConfig() {

        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(redisPropConfig.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisPropConfig.getMinIdle());
        genericObjectPoolConfig.setMaxTotal(redisPropConfig.getMaxActive());
        genericObjectPoolConfig.setMaxWaitMillis(redisPropConfig.getMaxWaitMillis());
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(redisPropConfig.getTimeBetweenEvictionRuns());

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder
                builder = LettucePoolingClientConfiguration.builder().
                commandTimeout(Duration.ofMillis(redisPropConfig.getTimeout()));
        builder.shutdownTimeout(Duration.ofMillis(redisPropConfig.getShutdownTimeOut()));

        builder.poolConfig(genericObjectPoolConfig);
        builder.clientResources(clientResources);

        if (redisCustomPropConfig.isUseCluster()){
            builder.clientOptions(redisClusterClientOptions());
        }
        LettuceClientConfiguration config = builder.build();
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

    private ClusterClientOptions redisClusterClientOptions() {

        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofSeconds(30))
                .enableAllAdaptiveRefreshTriggers()// 开启自适应刷新
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(30))
                .refreshTriggersReconnectAttempts(5)
                .build();
        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(15)))
                .autoReconnect(true)
                .maxRedirects(5)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
                .socketOptions(SocketOptions.builder().keepAlive(true).tcpNoDelay(true).build()).validateClusterNodeMembership(false)
                .build();
        return clusterClientOptions;
    }


    @Bean(value = "redisNodes")
    public RedisNodes redisNodes() {
        RedisNodes config = new RedisNodes();
        if(redisCustomPropConfig.isUseCluster()){
            config.setSentinels(redisPropConfig.getClusterNodes());
        }else if(redisCustomPropConfig.isUseSentinel()){
            config.setSentinels(redisPropConfig.getSentinelNodes());
        }
        return config;
    }
    /**
     *  设置 redis 数据默认过期时间，默认1天
     *  设置@cacheable 序列化方式
     * @return
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(){
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration = configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer));
        return configuration;
    }

    @Bean(name = "redisTemplate")
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        CustomStringRedisSerializer customStringRedisSerializer=new CustomStringRedisSerializer();
        stringRedisTemplate.setKeySerializer(customStringRedisSerializer);
        stringRedisTemplate.setValueSerializer(customStringRedisSerializer);
        stringRedisTemplate.setHashKeySerializer(customStringRedisSerializer);
        stringRedisTemplate.setHashValueSerializer(customStringRedisSerializer);
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        //预初始化连接
        prepareInit(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean(name = "reactiveStringRedisTemplate")
    @Primary
    @ConditionalOnMissingBean(name = "reactiveStringRedisTemplate")
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        ReactiveStringRedisTemplate reactiveStringRedisTemplate = new ReactiveStringRedisTemplate((ReactiveRedisConnectionFactory)redisConnectionFactory);
        return reactiveStringRedisTemplate;
    }

    /**
     * 预初始化连接
     * @param redisConnectionFactory
     */
    private void prepareInit(RedisConnectionFactory redisConnectionFactory) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RedisConnection connection = null;
                try {
                    connection = redisConnectionFactory.getConnection();
                    connection.time();
                } catch (Exception e) {
                } finally {
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }


    /**
     * 自定义缓存key生成策略
     * 使用方法 @Cacheable(keyGenerator="keyGenerator")
     * @return
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(JSON.toJSONString(obj).hashCode());
            }
            return sb.toString();
        };
    }

    @Bean
    public HashOperations<Object, String, Object> hashOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    @Bean
    public ValueOperations<String, String> valueOperations(RedisTemplate<String, String> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    @Bean
    public ListOperations<Object, Object> listOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    @Bean
    public SetOperations<Object, Object> setOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    @Bean
    public ZSetOperations<Object, Object> zSetOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}