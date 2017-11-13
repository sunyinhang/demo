package com.haiercash.spring.redis.core;

import com.haiercash.spring.redis.RedisProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public final class StringValueOperations extends AbstractStringOperations implements ValueOperations<String, String> {
    private final ValueOperations<String, String> operations;

    public StringValueOperations(RedisTemplate<String, String> template, RedisProperties properties, ValueOperations<String, String> operations) {
        super(template, properties);
        this.operations = operations;
    }

    @Override
    public void set(String key, String value) {
        this.operations.set(this.getKey(key), value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        this.operations.set(this.getKey(key), value, timeout, unit);
    }

    @Override
    public Boolean setIfAbsent(String key, String value) {
        return this.operations.setIfAbsent(this.getKey(key), value);
    }

    @Override
    public void multiSet(Map<? extends String, ? extends String> map) {
        this.operations.multiSet(this.getKeyMap(map));
    }

    @Override
    public Boolean multiSetIfAbsent(Map<? extends String, ? extends String> map) {
        return this.operations.multiSetIfAbsent(this.getKeyMap(map));
    }

    @Override
    public String get(Object key) {
        return this.operations.get(this.getKey((String) key));
    }

    @Override
    public String getAndSet(String key, String value) {
        return this.operations.getAndSet(this.getKey(key), value);
    }

    @Override
    public List<String> multiGet(Collection<String> keys) {
        return this.operations.multiGet(this.getKeys(keys));
    }

    @Override
    public Long increment(String key, long delta) {
        return this.operations.increment(this.getKey(key), delta);
    }

    @Override
    public Double increment(String key, double delta) {
        return this.operations.increment(this.getKey(key), delta);
    }

    @Override
    public Integer append(String key, String value) {
        return this.operations.append(this.getKey(key), value);
    }

    @Override
    public String get(String key, long start, long end) {
        return this.operations.get(this.getKey(key), start, end);
    }

    @Override
    public void set(String key, String value, long offset) {
        this.operations.set(this.getKey(key), value, offset);
    }

    @Override
    public Long size(String key) {
        return this.operations.size(this.getKey(key));
    }

    @Override
    public Boolean setBit(String key, long offset, boolean value) {
        return this.operations.setBit(this.getKey(key), offset, value);
    }

    @Override
    public Boolean getBit(String key, long offset) {
        return this.operations.getBit(this.getKey(key), offset);
    }
}
