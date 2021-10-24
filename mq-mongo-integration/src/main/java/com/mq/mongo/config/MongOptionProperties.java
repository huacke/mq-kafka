package com.mq.mongo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.data.mongodb.option")
public class MongOptionProperties {

    private Integer serverSelectionTimeout = 25000;

    private Integer maxPoolSize=500;
    private Integer minPoolSize=5;
    private Integer maxWaitTime = 15000;
    private Integer maxConnectionIdleTime = 0;
    private Integer maxConnectionLifeTime = 0;

    private Integer connectTimeout = 10000;
    private Integer socketTimeout = 10000;

    private Boolean sslEnabled = false;
    private Boolean sslInvalidHostNameAllowed = false;

    private Integer heartbeatFrequency = 10000;
    private Integer minHeartbeatFrequency = 500;
    private Integer localThreshold = 15;
}
