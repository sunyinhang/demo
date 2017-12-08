package com.haiercash.spring.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Component
public final class RedisTemplateProvider {
    private static RedisTemplateEx redisTemplate;
    @Autowired
    private RedisTemplateEx redisTemplateInstance;

    private RedisTemplateProvider() {
    }

    public static RedisTemplateEx getRedisTemplate() {
        return redisTemplate;
    }

    @PostConstruct
    private void init() {
        redisTemplate = this.redisTemplateInstance;
    }
}
