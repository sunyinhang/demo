package com.haiercash.spring.redis;

import com.haiercash.spring.redis.converter.PrefixStringRedisSerializer;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
public final class RedisTemplateEx extends RedisTemplate<String, String> {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final RedisProperties properties;

    public RedisTemplateEx(RedisConnectionFactory connectionFactory, RedisProperties properties) {
        this.properties = properties;
        PrefixStringRedisSerializer keySerializer = new PrefixStringRedisSerializer(DEFAULT_CHARSET, this.properties);
        StringRedisSerializer valueSerializer = new StringRedisSerializer(DEFAULT_CHARSET);
        this.setKeySerializer(keySerializer);
        this.setValueSerializer(valueSerializer);
        this.setHashKeySerializer(valueSerializer);
        this.setHashValueSerializer(valueSerializer);
        this.setConnectionFactory(connectionFactory);
        this.afterPropertiesSet();
    }

    public RedisProperties getProperties() {
        return this.properties;
    }

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return new DefaultStringRedisConnection(connection);
    }
}
