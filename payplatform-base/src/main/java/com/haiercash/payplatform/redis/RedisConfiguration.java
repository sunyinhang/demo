package com.haiercash.payplatform.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public final class RedisConfiguration {
    private RedisConfiguration() {
    }

    @Bean
    @DependsOn(value = "redisConfigurationProperties")
    private StringRedisTemplate redisTemplate() {
        return new RedisTemplateEx();
    }
}
