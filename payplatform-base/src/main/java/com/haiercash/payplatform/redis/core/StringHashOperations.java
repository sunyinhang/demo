package com.haiercash.payplatform.redis.core;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public class StringHashOperations<HK, KV> extends AbstractStringOperations implements HashOperations<String, HK, KV> {
    private final HashOperations<String, HK, KV> operations;

    public StringHashOperations(RedisTemplate<String, String> template, HashOperations<String, HK, KV> operations) {
        super(template);
        this.operations = operations;
    }

    @Override
    public Long delete(String key, Object... hashKeys) {
        return this.operations.delete(getKey(key), hashKeys);
    }

    @Override
    public Boolean hasKey(String key, Object hashKey) {
        return this.operations.hasKey(getKey(key), hashKey);
    }

    @Override
    public KV get(String key, Object hashKey) {
        return this.operations.get(getKey(key), hashKey);
    }

    @Override
    public List<KV> multiGet(String key, Collection<HK> hashKeys) {
        return this.operations.multiGet(getKey(key), hashKeys);
    }

    @Override
    public Long increment(String key, HK hashKey, long delta) {
        return this.operations.increment(getKey(key), hashKey, delta);
    }

    @Override
    public Double increment(String key, HK hashKey, double delta) {
        return this.operations.increment(getKey(key), hashKey, delta);
    }

    @Override
    public Set<HK> keys(String key) {
        return this.operations.keys(getKey(key));
    }

    @Override
    public Long size(String key) {
        return this.operations.size(getKey(key));
    }

    @Override
    public void putAll(String key, Map<? extends HK, ? extends KV> m) {
        this.operations.putAll(getKey(key), m);
    }

    @Override
    public void put(String key, HK hashKey, KV value) {
        this.operations.put(getKey(key), hashKey, value);
    }

    @Override
    public Boolean putIfAbsent(String key, HK hashKey, KV value) {
        return this.operations.putIfAbsent(getKey(key), hashKey, value);
    }

    @Override
    public List<KV> values(String key) {
        return this.operations.values(getKey(key));
    }

    @Override
    public Map<HK, KV> entries(String key) {
        return this.operations.entries(getKey(key));
    }

    @Override
    public Cursor<Map.Entry<HK, KV>> scan(String key, ScanOptions options) {
        return this.operations.scan(getKey(key), options);
    }
}
