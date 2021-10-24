package com.mq.boot.hook.impl;


import com.mq.boot.hook.BootPreStopedHook;
import com.mq.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * 下线之前针对注册中心的预下线钩子
 */
@Slf4j
public class BootPreStopedSrvHook extends BootPreStopedHook {

    @Override
    public void run() {
        try {
            Registration registration  = SpringUtils.getBean(Registration.class);
            ServiceRegistry serviceRegistry = SpringUtils.getBean(ServiceRegistry.class);
            serviceRegistry.deregister(registration);
            //eureka注册中心特殊处理（解决某些特殊情况下下线失效的问题）nacos则不存在此问题
            if(serviceRegistry.getClass().getSimpleName().toLowerCase().contains("eureka")){
                serviceRegistry.setStatus(registration,"DOWN");
            }
            log.info("BootPreStopedSrvHook 服务从注册中心下线成功！ ");
        } catch (Exception e) {
            log.error("BootPreStopedSrvHook 下线时发生错误！",e);
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
