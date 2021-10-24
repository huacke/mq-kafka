package com.mq.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * redis 自定义配置类
 * @author
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis.custom")
public class RedisCustomPropConfig {
	/*是否使用集群模式*/
	private boolean useCluster;
	/*是否使用哨兵模式*/
	private  boolean useSentinel;
}
