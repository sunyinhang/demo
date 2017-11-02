package com.haiercash.payplatform.redis.core;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public class StringSetOperations extends AbstractStringOperations implements SetOperations<String, String> {
    private final SetOperations<String, String> operations;

    public StringSetOperations(RedisTemplate<String, String> template, SetOperations<String, String> operations) {
        super(template);
        this.operations = operations;
    }

    @Override
    public Long add(String key, String... values) {
        return this.operations.add(getKey(key), values);
    }

    @Override
    public Long remove(String key, Object... values) {
        return this.operations.remove(getKey(key), values);
    }

    @Override
    public String pop(String key) {
        return this.operations.pop(getKey(key));
    }

    @Override
    public Boolean move(String key, String value, String destKey) {
        return this.operations.move(getKey(key), value, getKey(destKey));
    }

    @Override
    public Long size(String key) {
        return this.operations.size(getKey(key));
    }

    @Override
    public Boolean isMember(String key, Object o) {
        return this.operations.isMember(getKey(key), o);
    }

    @Override
    public Set<String> intersect(String key, String otherKey) {
        return this.operations.intersect(getKey(key), getKey(otherKey));
    }

    @Override
    public Set<String> intersect(String key, Collection<String> otherKeys) {
        return this.operations.intersect(getKey(key), getKeys(otherKeys));
    }

    @Override
    public Long intersectAndStore(String key, String otherKey, String destKey) {
        return this.operations.intersectAndStore(getKey(key), getKey(otherKey), getKey(destKey));
    }

    @Override
    public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.operations.intersectAndStore(getKey(key), getKeys(otherKeys), getKey(destKey));
    }

    @Override
    public Set<String> union(String key, String otherKey) {
        return this.operations.union(getKey(key), getKey(otherKey));
    }

    @Override
    public Set<String> union(String key, Collection<String> otherKeys) {
        return this.operations.union(getKey(key), getKeys(otherKeys));
    }

    @Override
    public Long unionAndStore(String key, String otherKey, String destKey) {
        return this.operations.unionAndStore(getKey(key), getKey(otherKey), getKey(destKey));
    }

    @Override
    public Long unionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.operations.unionAndStore(getKey(key), getKeys(otherKeys), getKey(destKey));
    }

    @Override
    public Set<String> difference(String key, String otherKey) {
        return this.operations.difference(getKey(key), getKey(otherKey));
    }

    @Override
    public Set<String> difference(String key, Collection<String> otherKeys) {
        return this.operations.difference(getKey(key), getKeys(otherKeys));
    }

    @Override
    public Long differenceAndStore(String key, String otherKey, String destKey) {
        return this.operations.differenceAndStore(getKey(key), getKey(otherKey), getKey(destKey));
    }

    @Override
    public Long differenceAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.operations.differenceAndStore(getKey(key), getKeys(otherKeys), getKey(destKey));
    }

    @Override
    public Set<String> members(String key) {
        return this.operations.members(getKey(key));
    }

    @Override
    public String randomMember(String key) {
        return this.operations.randomMember(getKey(key));
    }

    @Override
    public Set<String> distinctRandomMembers(String key, long count) {
        return this.operations.distinctRandomMembers(getKey(key), count);
    }

    @Override
    public List<String> randomMembers(String key, long count) {
        return this.operations.randomMembers(getKey(key), count);
    }

    @Override
    public Cursor<String> scan(String key, ScanOptions options) {
        return this.operations.scan(getKey(key), options);
    }
}
