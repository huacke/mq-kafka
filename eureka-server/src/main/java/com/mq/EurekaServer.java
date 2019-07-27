package com.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@EnableEurekaServer
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,DataSourceTransactionManagerAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
@Slf4j
public class EurekaServer extends SpringBootServletInitializer {


    private static volatile boolean running = true;

    public static void keepRunning(ApplicationContext applicationContext, String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                synchronized (EurekaServer.class) {
                    try {
                        ((ConfigurableApplicationContext) applicationContext).stop();
                    } catch (Exception e) {
                        log.error("EurekaServer停止时发生错误!", e);
                    }
                    running = false;
                    EurekaServer.class.notify();
                }
            }
        });
        log.info("启动EurekaServer服务成功!");
        synchronized (EurekaServer.class) {
            while (running) {
                try {
                    EurekaServer.class.wait();
                } catch (Exception e) {
                    log.error("EurekaServer服务发生错误!", e);
                }
            }
        }
        log.info("EurekaServer服务已停止!");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        return application.sources(EurekaServer.class);
    }


    public static void main(String[] args) {
        final ApplicationContext applicationContext = new SpringApplicationBuilder(EurekaServer.class).web(WebApplicationType.SERVLET).bannerMode(Banner.Mode.OFF).run(args);
        keepRunning(applicationContext, args);

    }

}
