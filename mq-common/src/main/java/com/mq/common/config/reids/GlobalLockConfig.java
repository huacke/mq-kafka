package com.mq.common.config.reids;

import com.mq.lock.redis.GlobalLockRedisFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @version v1.0
 * @ClassName GlobalLockConfig
 * @Description redis分布式锁配置
 */
@Configuration
public class GlobalLockConfig {

    @Autowired(required=true)
    private RedisTemplate redisTemplate;

    @Bean
    public GlobalLockRedisFactory globalLockRedisFactory() {
        GlobalLockRedisFactory globalLockRedisFactory = new GlobalLockRedisFactory();
        globalLockRedisFactory.setGlobalLockRedisTemplate(redisTemplate);
        return globalLockRedisFactory;
    }
}
