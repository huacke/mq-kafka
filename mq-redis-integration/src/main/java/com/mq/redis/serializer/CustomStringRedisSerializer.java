package com.mq.redis.serializer;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.serializer.RedisSerializer;
import java.nio.charset.Charset;

/**
 * 自定义字符序列化器(解决不是string类型转换出错的问题)
 */
public class CustomStringRedisSerializer implements RedisSerializer<Object> {

    private final Charset charset;

    private final String target = "\"";

    private final String replacement = "";

    public CustomStringRedisSerializer() {
        this(Charset.forName("UTF8"));
    }

    public CustomStringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) {
        return (bytes == null ? null : new String(bytes, charset));
    }

    @Override
    public byte[] serialize(Object object) {
        String str = null;
        if (object instanceof String) {
            str = (String) object;
        } else if (object instanceof Number) {
            str = object.toString();
        } else {
            str = JSON.toJSONString(object);
        }
        if (str == null) {
            return null;
        }
        str = str.replace(target, replacement);
        return str.getBytes(charset);
    }
}