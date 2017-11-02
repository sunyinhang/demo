package com.haiercash.payplatform.redis.core;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public class StringListOperations extends AbstractStringOperations implements ListOperations<String, String> {
    private final ListOperations<String, String> operations;

    public StringListOperations(RedisTemplate<String, String> template, ListOperations<String, String> operations) {
        super(template);
        this.operations = operations;
    }

    @Override
    public List<String> range(String key, long start, long end) {
        return this.operations.range(getKey(key), start, end);
    }

    @Override
    public void trim(String key, long start, long end) {
        this.operations.trim(getKey(key), start, end);
    }

    @Override
    public Long size(String key) {
        return this.operations.size(getKey(key));
    }

    @Override
    public Long leftPush(String key, String value) {
        return this.operations.leftPush(getKey(key), value);
    }

    @Override
    public Long leftPushAll(String key, String... values) {
        return this.operations.leftPushAll(getKey(key), values);
    }

    @Override
    public Long leftPushAll(String key, Collection<String> values) {
        return this.operations.leftPushAll(getKey(key), values);
    }

    @Override
    public Long leftPushIfPresent(String key, String value) {
        return this.operations.leftPushIfPresent(getKey(key), value);
    }

    @Override
    public Long leftPush(String key, String pivot, String value) {
        return this.operations.leftPush(getKey(key), pivot, value);
    }

    @Override
    public Long rightPush(String key, String value) {
        return this.operations.rightPush(getKey(key), value);
    }

    @Override
    public Long rightPushAll(String key, String... values) {
        return this.operations.rightPushAll(getKey(key), values);
    }

    @Override
    public Long rightPushAll(String key, Collection<String> values) {
        return this.operations.rightPushAll(getKey(key), values);
    }

    @Override
    public Long rightPushIfPresent(String key, String value) {
        return this.operations.rightPushIfPresent(getKey(key), value);
    }

    @Override
    public Long rightPush(String key, String pivot, String value) {
        return this.operations.rightPush(getKey(key), pivot, value);
    }

    @Override
    public void set(String key, long index, String value) {
        this.operations.set(getKey(key), index, value);
    }

    @Override
    public Long remove(String key, long count, Object value) {
        return this.operations.remove(getKey(key), count, value);
    }

    @Override
    public String index(String key, long index) {
        return this.operations.index(getKey(key), index);
    }

    @Override
    public String leftPop(String key) {
        return this.operations.leftPop(getKey(key));
    }

    @Override
    public String leftPop(String key, long timeout, TimeUnit unit) {
        return this.operations.leftPop(getKey(key), timeout, unit);
    }

    @Override
    public String rightPop(String key) {
        return this.operations.rightPop(getKey(key));
    }

    @Override
    public String rightPop(String key, long timeout, TimeUnit unit) {
        return this.operations.rightPop(getKey(key), timeout, unit);
    }

    @Override
    public String rightPopAndLeftPush(String sourceKey, String destinationKey) {
        return this.operations.rightPopAndLeftPush(getKey(sourceKey), getKey(destinationKey));
    }

    @Override
    public String rightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
        return this.operations.rightPopAndLeftPush(getKey(sourceKey), getKey(destinationKey), timeout, unit);
    }
}
