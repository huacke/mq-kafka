package com.walton.mq;

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
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.mq.**")
@EnableDiscoveryClient
@EnableFeignClients("com.mq.**")
@Slf4j
public class MQServiceTR extends SpringBootServletInitializer {


    private static volatile boolean running = true;

    public static void keepRunning(ApplicationContext applicationContext, String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                synchronized (MQServiceTR.class) {
                    try {
                        ((ConfigurableApplicationContext) applicationContext).stop();
                    } catch (Exception e) {
                        log.error("MQServiceTR停止时发生错误!", e);
                    }
                    running = false;
                    MQServiceTR.class.notify();
                }
            }
        });
        log.info("启动MQServiceTR服务成功!");
        synchronized (MQServiceTR.class) {
            while (running) {
                try {
                    MQServiceTR.class.wait();
                } catch (Exception e) {
                    log.error("MQServiceTR服务发生错误!", e);
                }
            }
        }
        log.info("MQServiceTR服务已停止!");
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MQServiceTR.class);
    }

    public static void main(String[] args) {
        final ApplicationContext applicationContext = new SpringApplicationBuilder(MQServiceTR.class).web(WebApplicationType.SERVLET).bannerMode(Banner.Mode.OFF).run(args);
        keepRunning(applicationContext, args);

    }

}
