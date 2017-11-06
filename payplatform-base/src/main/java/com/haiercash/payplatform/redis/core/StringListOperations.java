package com.haiercash.payplatform.redis.core;

import com.haiercash.payplatform.redis.RedisProperties;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public final class StringListOperations extends AbstractStringOperations implements ListOperations<String, String> {
    private final ListOperations<String, String> operations;

    public StringListOperations(RedisTemplate<String, String> template, RedisProperties properties, ListOperations<String, String> operations) {
        super(template, properties);
        this.operations = operations;
    }

    @Override
    public List<String> range(String key, long start, long end) {
        return this.operations.range(this.getKey(key), start, end);
    }

    @Override
    public void trim(String key, long start, long end) {
        this.operations.trim(this.getKey(key), start, end);
    }

    @Override
    public Long size(String key) {
        return this.operations.size(this.getKey(key));
    }

    @Override
    public Long leftPush(String key, String value) {
        return this.operations.leftPush(this.getKey(key), value);
    }

    @Override
    public Long leftPushAll(String key, String... values) {
        return this.operations.leftPushAll(this.getKey(key), values);
    }

    @Override
    public Long leftPushAll(String key, Collection<String> values) {
        return this.operations.leftPushAll(this.getKey(key), values);
    }

    @Override
    public Long leftPushIfPresent(String key, String value) {
        return this.operations.leftPushIfPresent(this.getKey(key), value);
    }

    @Override
    public Long leftPush(String key, String pivot, String value) {
        return this.operations.leftPush(this.getKey(key), pivot, value);
    }

    @Override
    public Long rightPush(String key, String value) {
        return this.operations.rightPush(this.getKey(key), value);
    }

    @Override
    public Long rightPushAll(String key, String... values) {
        return this.operations.rightPushAll(this.getKey(key), values);
    }

    @Override
    public Long rightPushAll(String key, Collection<String> values) {
        return this.operations.rightPushAll(this.getKey(key), values);
    }

    @Override
    public Long rightPushIfPresent(String key, String value) {
        return this.operations.rightPushIfPresent(this.getKey(key), value);
    }

    @Override
    public Long rightPush(String key, String pivot, String value) {
        return this.operations.rightPush(this.getKey(key), pivot, value);
    }

    @Override
    public void set(String key, long index, String value) {
        this.operations.set(this.getKey(key), index, value);
    }

    @Override
    public Long remove(String key, long count, Object value) {
        return this.operations.remove(this.getKey(key), count, value);
    }

    @Override
    public String index(String key, long index) {
        return this.operations.index(this.getKey(key), index);
    }

    @Override
    public String leftPop(String key) {
        return this.operations.leftPop(this.getKey(key));
    }

    @Override
    public String leftPop(String key, long timeout, TimeUnit unit) {
        return this.operations.leftPop(this.getKey(key), timeout, unit);
    }

    @Override
    public String rightPop(String key) {
        return this.operations.rightPop(this.getKey(key));
    }

    @Override
    public String rightPop(String key, long timeout, TimeUnit unit) {
        return this.operations.rightPop(this.getKey(key), timeout, unit);
    }

    @Override
    public String rightPopAndLeftPush(String sourceKey, String destinationKey) {
        return this.operations.rightPopAndLeftPush(this.getKey(sourceKey), this.getKey(destinationKey));
    }

    @Override
    public String rightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
        return this.operations.rightPopAndLeftPush(this.getKey(sourceKey), this.getKey(destinationKey), timeout, unit);
    }
}
