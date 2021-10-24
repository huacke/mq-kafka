package com.mq;

import com.mq.boot.bootstrap.builder.BootstrapBuilder;
import com.mq.kafka.hook.StopKafkaProducerHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients("com.mq.**")
@ComponentScan(basePackages = "com.mq.**")
public class MQService  extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MQService.class);
    }

    public static void main(String[] args) {
        //下线流程： 1，先把服务从注册中心下线 -》 2，会有一个延时等待默认15s，等服务集群的本地服务注册中心缓存失效-》3，关闭生产者-》4 stop spring 容器
        new BootstrapBuilder()
                .runArgs(args)
                .preStopHook(new StopKafkaProducerHook())
                .build()
                .start();
    }

}
