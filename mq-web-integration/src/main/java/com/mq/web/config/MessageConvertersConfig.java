package com.mq.web.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 序列化配置
 */
@Configuration
public class MessageConvertersConfig implements WebMvcConfigurer {


    WebMvcConfigurationSupportProxy webMvcConfigurationSupportProxy = new WebMvcConfigurationSupportProxy();


    @Autowired
    private RestTemplateBuilder builder;

    protected List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

    public HttpMessageConverter fastJsonHttpMessageConverter = fastJsonHttpMessageConverter();

    private HttpMessageConverter fastJsonHttpMessageConverter() {
        // 1、需要先定义一个 convert 转换消息的对象;
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        //2、添加fastJson 的配置信息，比如：是否要格式化返回的json数据;
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.IgnoreNonFieldGetter,
                SerializerFeature.WriteMapNullValue
        );
        //2-1 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastMediaTypes.add(MediaType.TEXT_PLAIN);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);
        //2-2 处理日期格式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //3、在convert中添加配置信息.
        fastConverter.setFastJsonConfig(fastJsonConfig);
        HttpMessageConverter<?> converter = fastConverter;
        return converter;

    }

    public HttpMessageConverter<String> stringConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converter.setWriteAcceptCharset(false);
        return converter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> extendConverters = new ArrayList<>();
        extendConverters.add(stringConverter());
        extendConverters.add(fastJsonHttpMessageConverter);
        extendConverters.addAll(converters);
        converters.clear();
        converters.addAll(extendConverters);
        messageConverters.addAll(converters);
    }


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = builder.build();
        restTemplate.getMessageConverters().clear();
        if (messageConverters.isEmpty()) {
            restTemplate.getMessageConverters().add(stringConverter());
            restTemplate.getMessageConverters().add(fastJsonHttpMessageConverter());
            webMvcConfigurationSupportProxy.addDefaultMessageConverters(restTemplate.getMessageConverters());
        } else {
            restTemplate.getMessageConverters().addAll(messageConverters);
        }
        return restTemplate;
    }

    public static class WebMvcConfigurationSupportProxy extends WebMvcConfigurationSupport {
        public void addDefaultMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
            addDefaultHttpMessageConverters(messageConverters);
        }

    }

}
