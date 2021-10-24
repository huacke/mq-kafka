package com.data;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 代码生成服务
 */
@SpringBootApplication
@MapperScan("com.data.dao")
public class GenApp {
	public static void main(String[] args) {
		SpringApplication.run(GenApp.class, args);
	}
}
