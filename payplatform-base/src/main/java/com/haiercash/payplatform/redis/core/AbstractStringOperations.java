package com.haiercash.payplatform.redis.core;

import com.haiercash.payplatform.redis.RedisProperties;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public abstract class AbstractStringOperations {
    private final RedisTemplate<String, String> template;
    private final RedisProperties properties;

    protected AbstractStringOperations(RedisTemplate<String, String> template, RedisProperties properties) {
        this.template = template;
        this.properties = properties;
    }

    protected String getKey(String key) {
        return this.properties.getKey(key);
    }

    protected String[] getKeys(String[] keys) {
        return this.properties.getKeys(keys);
    }

    protected Collection<String> getKeys(Collection<String> keys) {
        return this.properties.getKeys(keys);
    }

    protected <T> Map<? extends String, T> getKeyMap(Map<? extends String, T> map) {
        return this.properties.getKeyMap(map);
    }

    public RedisOperations<String, String> getOperations() {
        return this.template;
    }
}
