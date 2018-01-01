package com.haiercash.spring.redis.converter;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.redis.RedisProperties;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by 许崇雷 on 2017-12-07.
 */
public final class PrefixStringRedisSerializer implements RedisSerializer<String> {
    private final Charset charset;
    private final RedisProperties properties;

    public PrefixStringRedisSerializer(Charset charset, RedisProperties properties) {
        Assert.notNull(charset, "charset must not be null");
        Assert.notNull(properties, "properties must not be null");
        this.charset = charset;
        this.properties = properties;
    }

    public String deserialize(byte[] bytes) {
        return bytes == null ? StringUtils.EMPTY : this.properties.removePrefix(new String(bytes, this.charset));
    }

    public byte[] serialize(String key) {
        return this.properties.addPrefix(key).getBytes(StandardCharsets.UTF_8);
    }
}
