package com.haiercash.spring.redis.core;

import com.haiercash.spring.redis.RedisProperties;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public final class StringHyperLogLogOperations extends AbstractStringOperations implements HyperLogLogOperations<String, String> {
    private final HyperLogLogOperations<String, String> operations;

    public StringHyperLogLogOperations(RedisTemplate<String, String> template, RedisProperties properties, HyperLogLogOperations<String, String> operations) {
        super(template, properties);
        this.operations = operations;
    }

    @Override
    public Long add(String key, String... values) {
        return this.operations.add(this.getKey(key), values);
    }

    @Override
    public Long size(String... keys) {
        return this.operations.size(this.getKeys(keys));
    }

    @Override
    public Long union(String destination, String... sourceKeys) {
        return this.operations.union(this.getKey(destination), this.getKeys(sourceKeys));
    }

    @Override
    public void delete(String key) {
        this.operations.delete(this.getKey(key));
    }
}
