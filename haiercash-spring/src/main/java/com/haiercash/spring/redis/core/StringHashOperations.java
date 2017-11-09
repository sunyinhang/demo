package com.haiercash.spring.redis.core;

import com.haiercash.spring.redis.RedisProperties;
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
public final class StringHashOperations<HK, KV> extends AbstractStringOperations implements HashOperations<String, HK, KV> {
    private final HashOperations<String, HK, KV> operations;

    public StringHashOperations(RedisTemplate<String, String> template, RedisProperties properties, HashOperations<String, HK, KV> operations) {
        super(template, properties);
        this.operations = operations;
    }

    @Override
    public Long delete(String key, Object... hashKeys) {
        return this.operations.delete(this.getKey(key), hashKeys);
    }

    @Override
    public Boolean hasKey(String key, Object hashKey) {
        return this.operations.hasKey(this.getKey(key), hashKey);
    }

    @Override
    public KV get(String key, Object hashKey) {
        return this.operations.get(this.getKey(key), hashKey);
    }

    @Override
    public List<KV> multiGet(String key, Collection<HK> hashKeys) {
        return this.operations.multiGet(this.getKey(key), hashKeys);
    }

    @Override
    public Long increment(String key, HK hashKey, long delta) {
        return this.operations.increment(this.getKey(key), hashKey, delta);
    }

    @Override
    public Double increment(String key, HK hashKey, double delta) {
        return this.operations.increment(this.getKey(key), hashKey, delta);
    }

    @Override
    public Set<HK> keys(String key) {
        return this.operations.keys(this.getKey(key));
    }

    @Override
    public Long size(String key) {
        return this.operations.size(this.getKey(key));
    }

    @Override
    public void putAll(String key, Map<? extends HK, ? extends KV> m) {
        this.operations.putAll(this.getKey(key), m);
    }

    @Override
    public void put(String key, HK hashKey, KV value) {
        this.operations.put(this.getKey(key), hashKey, value);
    }

    @Override
    public Boolean putIfAbsent(String key, HK hashKey, KV value) {
        return this.operations.putIfAbsent(this.getKey(key), hashKey, value);
    }

    @Override
    public List<KV> values(String key) {
        return this.operations.values(this.getKey(key));
    }

    @Override
    public Map<HK, KV> entries(String key) {
        return this.operations.entries(this.getKey(key));
    }

    @Override
    public Cursor<Map.Entry<HK, KV>> scan(String key, ScanOptions options) {
        return this.operations.scan(this.getKey(key), options);
    }
}
