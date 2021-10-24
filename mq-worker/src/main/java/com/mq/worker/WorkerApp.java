package com.mq.worker;

import com.mq.boot.bootstrap.builder.BootstrapBuilder;
import com.mq.consumer.kafka.hook.StopKafkaConsumerWorkerHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
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

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WorkerApp.class);
	}

	public static void main(String[] args) {
		//下线流程： 1，停止kafka消费者 -》2，把服务从注册中心下线 -》 3，会有一个延时等待默认15s，等服务集群的本地服务注册中心缓存失效-》4 stop spring 容器
		new BootstrapBuilder()
				//容器关闭之前先释放worker资源
				.preStopHook(new StopKafkaConsumerWorkerHook())
				.runArgs(args)
				.build()
				.start();
	}
}