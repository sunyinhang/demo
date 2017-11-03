package com.haiercash.payplatform.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Component
public final class RedisTemplateProvider {
    private static StringRedisTemplate redisTemplate;
    private static RedisProperties redisProperties;
    @Autowired
    private StringRedisTemplate redisTemplateInstance;
    @Autowired
    private RedisProperties redisPropertiesInstance;

    private RedisTemplateProvider() {
    }

    public static StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public static RedisProperties getRedisProperties() {
        return redisProperties;
    }

    @PostConstruct
    private void init() {
        redisTemplate = this.redisTemplateInstance;
        redisProperties = this.redisPropertiesInstance;
    }
}
