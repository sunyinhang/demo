package com.haiercash.payplatform.redis.core;

import com.haiercash.payplatform.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.Set;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public final class StringZSetOperations extends AbstractStringOperations implements ZSetOperations<String, String> {
    private final ZSetOperations<String, String> operations;

    public StringZSetOperations(RedisTemplate<String, String> template, RedisProperties properties, ZSetOperations<String, String> operations) {
        super(template, properties);
        this.operations = operations;
    }

    @Override
    public Boolean add(String key, String value, double score) {
        return this.operations.add(this.getKey(key), value, score);
    }

    @Override
    public Long add(String key, Set<TypedTuple<String>> typedTuples) {
        return this.operations.add(this.getKey(key), typedTuples);
    }

    @Override
    public Long remove(String key, Object... values) {
        return this.operations.remove(this.getKey(key), values);
    }

    @Override
    public Double incrementScore(String key, String value, double delta) {
        return this.operations.incrementScore(this.getKey(key), value, delta);
    }

    @Override
    public Long rank(String key, Object o) {
        return this.operations.rank(this.getKey(key), o);
    }

    @Override
    public Long reverseRank(String key, Object o) {
        return this.operations.reverseRank(this.getKey(key), o);
    }

    @Override
    public Set<String> range(String key, long start, long end) {
        return this.operations.range(this.getKey(key), start, end);
    }

    @Override
    public Set<TypedTuple<String>> rangeWithScores(String key, long start, long end) {
        return this.operations.rangeWithScores(this.getKey(key), start, end);
    }

    @Override
    public Set<String> rangeByScore(String key, double min, double max) {
        return this.operations.rangeByScore(this.getKey(key), min, max);
    }

    @Override
    public Set<TypedTuple<String>> rangeByScoreWithScores(String key, double min, double max) {
        return this.operations.rangeByScoreWithScores(this.getKey(key), min, max);
    }

    @Override
    public Set<String> rangeByScore(String key, double min, double max, long offset, long count) {
        return this.operations.rangeByScore(this.getKey(key), min, max, offset, count);
    }

    @Override
    public Set<TypedTuple<String>> rangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        return this.operations.rangeByScoreWithScores(this.getKey(key), min, max, offset, count);
    }

    @Override
    public Set<String> reverseRange(String key, long start, long end) {
        return this.operations.reverseRange(this.getKey(key), start, end);
    }

    @Override
    public Set<TypedTuple<String>> reverseRangeWithScores(String key, long start, long end) {
        return this.operations.reverseRangeWithScores(this.getKey(key), start, end);
    }

    @Override
    public Set<String> reverseRangeByScore(String key, double min, double max) {
        return this.operations.reverseRangeByScore(this.getKey(key), min, max);
    }

    @Override
    public Set<TypedTuple<String>> reverseRangeByScoreWithScores(String key, double min, double max) {
        return this.operations.reverseRangeByScoreWithScores(this.getKey(key), min, max);
    }

    @Override
    public Set<String> reverseRangeByScore(String key, double min, double max, long offset, long count) {
        return this.operations.reverseRangeByScore(this.getKey(key), min, max, offset, count);
    }

    @Override
    public Set<TypedTuple<String>> reverseRangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        return this.operations.reverseRangeByScoreWithScores(this.getKey(key), min, max, offset, count);
    }

    @Override
    public Long count(String key, double min, double max) {
        return this.operations.count(this.getKey(key), min, max);
    }

    @Override
    public Long size(String key) {
        return this.operations.size(this.getKey(key));
    }

    @Override
    public Long zCard(String key) {
        return this.operations.zCard(this.getKey(key));
    }

    @Override
    public Double score(String key, Object o) {
        return this.operations.score(this.getKey(key), o);
    }

    @Override
    public Long removeRange(String key, long start, long end) {
        return this.operations.removeRange(this.getKey(key), start, end);
    }

    @Override
    public Long removeRangeByScore(String key, double min, double max) {
        return this.operations.removeRangeByScore(this.getKey(key), min, max);
    }

    @Override
    public Long unionAndStore(String key, String otherKey, String destKey) {
        return this.operations.unionAndStore(this.getKey(key), this.getKey(otherKey), this.getKey(destKey));
    }

    @Override
    public Long unionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.operations.unionAndStore(this.getKey(key), this.getKeys(otherKeys), this.getKey(destKey));
    }

    @Override
    public Long intersectAndStore(String key, String otherKey, String destKey) {
        return this.operations.intersectAndStore(this.getKey(key), this.getKey(otherKey), this.getKey(destKey));
    }

    @Override
    public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.operations.intersectAndStore(this.getKey(key), this.getKeys(otherKeys), this.getKey(destKey));
    }

    @Override
    public Cursor<TypedTuple<String>> scan(String key, ScanOptions options) {
        return this.operations.scan(this.getKey(key), options);
    }

    @Override
    public Set<String> rangeByLex(String key, RedisZSetCommands.Range range) {
        return this.operations.rangeByLex(this.getKey(key), range);
    }

    @Override
    public Set<String> rangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
        return this.operations.rangeByLex(this.getKey(key), range, limit);
    }
}
