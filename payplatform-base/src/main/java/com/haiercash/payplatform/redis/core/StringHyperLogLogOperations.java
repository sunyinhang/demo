package com.haiercash.payplatform.redis.core;

import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public class StringHyperLogLogOperations extends AbstractStringOperations implements HyperLogLogOperations<String, String> {
    private final HyperLogLogOperations<String, String> operations;

    public StringHyperLogLogOperations(RedisTemplate<String, String> template, HyperLogLogOperations<String, String> operations) {
        super(template);
        this.operations = operations;
    }

    @Override
    public Long add(String key, String... values) {
        return this.operations.add(getKey(key), values);
    }

    @Override
    public Long size(String... keys) {
        return this.operations.size(getKeys(keys));
    }

    @Override
    public Long union(String destination, String... sourceKeys) {
        return this.operations.union(getKey(destination), getKeys(sourceKeys));
    }

    @Override
    public void delete(String key) {
        this.operations.delete(getKey(key));
    }
}
