package com.mq.web.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version v1.0
 * @ClassName InitConfiguration
 * @Description 初始化配置
 */
@Configuration
@Slf4j
public class InitConfiguration {
    @Bean
    public InitBean initBean() {
        return new InitBean();
    }
}

