package com.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients("com.mq.**")
@ComponentScan(basePackages = "com.mq.**")
public class MQService  extends SpringBootServletInitializer {

    private static volatile boolean running = true;

    public static void keepRunning(ApplicationContext applicationContext, String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                synchronized (MQService.class) {
                    try {
                        ((ConfigurableApplicationContext) applicationContext).stop();
                    } catch (Exception e) {
                        log.error("MQService停止时发生错误!", e);
                    }
                    running = false;
                    MQService.class.notify();
                }
            }
        });
        log.info("启动MQService服务成功!");
        synchronized (MQService.class) {
            while (running) {
                try {
                    MQService.class.wait();
                } catch (Exception e) {
                    log.error("MQService服务发生错误!", e);
                }
            }
        }
        log.info("MQService服务已停止!");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        return application.sources(MQService.class);
    }

    public static void main(String[] args) {
        final ApplicationContext applicationContext = new SpringApplicationBuilder(MQService.class).web(WebApplicationType.SERVLET).bannerMode(Banner.Mode.OFF).run(args);
        keepRunning(applicationContext, args);

    }

}
