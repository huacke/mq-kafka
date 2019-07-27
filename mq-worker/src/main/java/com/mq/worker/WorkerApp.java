package com.mq.worker;

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
import org.springframework.transaction.annotation.EnableTransactionManagement;
/**
 * WorkerApp 启动类
 */
@Slf4j
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients("com.mq.**")
@ComponentScan(basePackages = "com.mq.**")
public class WorkerApp extends SpringBootServletInitializer {


	private static volatile boolean running = true;

	public static void keepRunning(ApplicationContext applicationContext, String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				synchronized (WorkerApp.class) {
					try {
						((ConfigurableApplicationContext) applicationContext).stop();
					} catch (Exception e) {
						log.error("WorkerApp停止时发生错误!", e);
					}
					running = false;
					WorkerApp.class.notify();
				}
			}
		});

		log.info("启动WorkerApp服务成功!");

		synchronized (WorkerApp.class) {
			while (running) {
				try {
					WorkerApp.class.wait();
				} catch (Exception e) {
					log.error("WorkerApp服务发生错误!", e);
				}
			}
		}
		log.info("WorkerApp服务已停止!");
	}


	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

		return application.sources(WorkerApp.class);
	}


	public static void main(String[] args) {
		final ApplicationContext applicationContext = new SpringApplicationBuilder(WorkerApp.class).web(WebApplicationType.SERVLET).bannerMode(Banner.Mode.OFF).run(args);
		keepRunning(applicationContext, args);

	}
}