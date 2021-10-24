package com.mq.web.feign;

import org.springframework.cloud.netflix.ribbon.RibbonApplicationContextInitializer;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import java.util.List;

/**
 * 重载Ribbon饿加载初始化过程
 */
public class CustomRibbonApplicationContextInitializer extends RibbonApplicationContextInitializer {

    public CustomRibbonApplicationContextInitializer(SpringClientFactory springClientFactory, List<String> clientNames) {
        super(springClientFactory, clientNames);
    }
    @Override
    protected void initialize() {
        super.initialize();
    }
}
