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
    private static RedisConfigurationProperties redisConfigurationProperties;
    @Autowired
    private StringRedisTemplate redisTemplateInstance;
    @Autowired
    private RedisConfigurationProperties redisConfigurationPropertiesInstance;

    private RedisTemplateProvider() {
    }

    public static StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public static RedisConfigurationProperties getRedisConfigurationProperties() {
        return redisConfigurationProperties;
    }

    @PostConstruct
    private void init() {
        redisTemplate = this.redisTemplateInstance;
        redisConfigurationProperties = this.redisConfigurationPropertiesInstance;
    }
}
