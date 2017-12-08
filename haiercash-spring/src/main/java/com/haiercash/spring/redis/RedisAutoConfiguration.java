package com.haiercash.spring.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Created by 许崇雷 on 2017-11-03.
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {
    private final RedisProperties properties;

    public RedisAutoConfiguration(RedisProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    RedisTemplateEx redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new RedisTemplateEx(redisConnectionFactory, this.properties);
    }
}
