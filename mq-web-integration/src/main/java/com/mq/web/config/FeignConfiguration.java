package com.mq.web.config;

import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import com.mq.web.feign.FeignHystrixConcurrencyStrategy;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version v1.0
 * @ClassName FeginConfiguration
 * @Description Fegin配置
 */
@Configuration
@Slf4j
public class FeignConfiguration {

    @Autowired
    MessageConvertersConfig convertersConfig;

    @Bean
    public FeignHystrixConcurrencyStrategy feignHystrixConcurrencyStrategy() {
        return new FeignHystrixConcurrencyStrategy();
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(feignHttpMessageConverter());
    }

    @Bean
    public Decoder feignDecoder() {
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverter()));
    }

    private ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
        final HttpMessageConverters httpMessageConverters =new HttpMessageConverters(convertersConfig.stringConverter(),convertersConfig.fastJsonHttpMessageConverter);
        return () -> httpMessageConverters;
    }

    @Bean
    public ErrorDecoder errorDecoder(){
        return new FeginErrorDecoder();
    }

    @Slf4j
    public static class FeginErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, Response response) {
            Exception exception = null;
            try {
                String body = Util.toString(response.body().asReader());
                exception = new BussinessException(ErrorCode.SYSTEM_ERROR,body);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return exception;
        }
    }

}

