package com.mq.web.feign;

import com.mq.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.netflix.ribbon.RibbonApplicationContextInitializer;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import java.util.*;

/**
 * @version v1.0
 * @ClassName RibbonEagerLoadConfiguration
 * @Description 当容器启动时饿加载所有的FeignClient Ribbon实例(省去配置文件里指定服务名称，缓解项目第一次启动加载缓慢的问题)
 */
@Configuration
@ConditionalOnProperty("ribbon.eager-load.enabled")
@Slf4j
public class RibbonEagerLoadConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private SpringClientFactory springClientFactory;

    @Autowired
    private RibbonApplicationContextInitializer ribbonApplicationContextInitializer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(FeignClient.class);
        Set<String> clientNames = new HashSet<>();
        List<HealthSupport> feginHealthBeans =new ArrayList<>();
        beans.entrySet().forEach(it -> {
            try {
                String clazzName = it.getKey();
                Class<?> clientClazz = Class.forName(clazzName);
                FeignClient clientAnnotation = clientClazz.getAnnotation(FeignClient.class);
                clientNames.add(clientAnnotation.name());
                try {
                    Object bean = SpringUtils.getBean(clientClazz);
                    if (bean instanceof HealthSupport) {
                        HealthSupport healthSupport = (HealthSupport) bean;
                        feginHealthBeans.add(healthSupport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
            }
        });
        if (CollectionUtils.isEmpty(clientNames)) {
            return;
        }
        List<String> clients = new ArrayList<>(clientNames);
        CustomRibbonApplicationContextInitializer initializer = new CustomRibbonApplicationContextInitializer(springClientFactory, clients);
        initializer.initialize();
        feginHealthBeans.forEach(it->{
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        it.health();
                    }
                }).start();
            } catch (Throwable e) {
            }
        });
    }
}

