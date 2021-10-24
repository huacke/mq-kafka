package com.mq.web.config;

import com.mq.web.feign.rule.HealthIPing;
import com.netflix.loadbalancer.IPing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version v1.0
 * @ClassName RibbonConfiguration
 * @Description ribbon配置
 */
@Configuration
public class RibbonConfiguration {
    @Bean
    public IPing iPing() {
        return new HealthIPing();
    }
}
