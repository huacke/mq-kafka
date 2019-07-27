package com.mq.common.config.reids;

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
	private boolean useCluster;
	private  boolean useSentinel;
}
