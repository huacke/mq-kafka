package com.mq.common.config.reids;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * redis 配置类
 *
 * @author
 */
@Data
@Configuration
public class RedisPropConfig {
    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.sentinel.nodes:}")
    private List<String> sentinelNodes;

    @Value("${spring.redis.sentinel.master:}")
    private String master;

    @Value("${spring.redis.cluster.nodes:}")
    private List<String> clusterNodes;

    @Value("${spring.redis.cluster.maxRedirects:5}")
    private Integer maxRedirects;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.time-between-eviction-runs}")
    private long timeBetweenEvictionRuns;


}
