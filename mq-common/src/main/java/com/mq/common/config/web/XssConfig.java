package com.mq.common.config.web;

import com.mq.web.xss.XssFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 */
@Configuration
public class XssConfig {
	@Bean
	public FilterRegistrationBean xssFilterRegistration() {
		XssFilter xssFilter = new XssFilter();
		FilterRegistrationBean registration = new FilterRegistrationBean(xssFilter);
		registration.setOrder(Integer.MAX_VALUE);
		registration.addUrlPatterns("/*");
		return registration;
	}

}
