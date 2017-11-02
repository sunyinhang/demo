package com.haiercash.payplatform.redis.core;

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
public class StringZSetOperations extends AbstractStringOperations implements ZSetOperations<String, String> {
    private final ZSetOperations<String, String> operations;

    public StringZSetOperations(RedisTemplate<String, String> template, ZSetOperations<String, String> operations) {
        super(template);
        this.operations = operations;
    }

    @Override
    public Boolean add(String key, String value, double score) {
        return this.operations.add(getKey(key), value, score);
    }

    @Override
    public Long add(String key, Set<TypedTuple<String>> typedTuples) {
        return this.operations.add(getKey(key), typedTuples);
    }

    @Override
    public Long remove(String key, Object... values) {
        return this.operations.remove(getKey(key), values);
    }

    @Override
    public Double incrementScore(String key, String value, double delta) {
        return this.operations.incrementScore(getKey(key), value, delta);
    }

    @Override
    public Long rank(String key, Object o) {
        return this.operations.rank(getKey(key), o);
    }

    @Override
    public Long reverseRank(String key, Object o) {
        return this.operations.reverseRank(getKey(key), o);
    }

    @Override
    public Set<String> range(String key, long start, long end) {
        return this.operations.range(getKey(key), start, end);
    }

    @Override
    public Set<TypedTuple<String>> rangeWithScores(String key, long start, long end) {
        return this.operations.rangeWithScores(getKey(key), start, end);
    }

    @Override
    public Set<String> rangeByScore(String key, double min, double max) {
        return this.operations.rangeByScore(getKey(key), min, max);
    }

    @Override
    public Set<TypedTuple<String>> rangeByScoreWithScores(String key, double min, double max) {
        return this.operations.rangeByScoreWithScores(getKey(key), min, max);
    }

    @Override
    public Set<String> rangeByScore(String key, double min, double max, long offset, long count) {
        return this.operations.rangeByScore(getKey(key), min, max, offset, count);
    }

    @Override
    public Set<TypedTuple<String>> rangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        return this.operations.rangeByScoreWithScores(getKey(key), min, max, offset, count);
    }

    @Override
    public Set<String> reverseRange(String key, long start, long end) {
        return this.operations.reverseRange(getKey(key), start, end);
    }

    @Override
    public Set<TypedTuple<String>> reverseRangeWithScores(String key, long start, long end) {
        return this.operations.reverseRangeWithScores(getKey(key), start, end);
    }

    @Override
    public Set<String> reverseRangeByScore(String key, double min, double max) {
        return this.operations.reverseRangeByScore(getKey(key), min, max);
    }

    @Override
    public Set<TypedTuple<String>> reverseRangeByScoreWithScores(String key, double min, double max) {
        return this.operations.reverseRangeByScoreWithScores(getKey(key), min, max);
    }

    @Override
    public Set<String> reverseRangeByScore(String key, double min, double max, long offset, long count) {
        return this.operations.reverseRangeByScore(getKey(key), min, max, offset, count);
    }

    @Override
    public Set<TypedTuple<String>> reverseRangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        return this.operations.reverseRangeByScoreWithScores(getKey(key), min, max, offset, count);
    }

    @Override
    public Long count(String key, double min, double max) {
        return this.operations.count(getKey(key), min, max);
    }

    @Override
    public Long size(String key) {
        return this.operations.size(getKey(key));
    }

    @Override
    public Long zCard(String key) {
        return this.operations.zCard(getKey(key));
    }

    @Override
    public Double score(String key, Object o) {
        return this.operations.score(getKey(key), o);
    }

    @Override
    public Long removeRange(String key, long start, long end) {
        return this.operations.removeRange(getKey(key), start, end);
    }

    @Override
    public Long removeRangeByScore(String key, double min, double max) {
        return this.operations.removeRangeByScore(getKey(key), min, max);
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
    public Long intersectAndStore(String key, String otherKey, String destKey) {
        return this.operations.intersectAndStore(getKey(key), getKey(otherKey), getKey(destKey));
    }

    @Override
    public Long intersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return this.operations.intersectAndStore(getKey(key), getKeys(otherKeys), getKey(destKey));
    }

    @Override
    public Cursor<TypedTuple<String>> scan(String key, ScanOptions options) {
        return this.operations.scan(getKey(key), options);
    }

    @Override
    public Set<String> rangeByLex(String key, RedisZSetCommands.Range range) {
        return this.operations.rangeByLex(getKey(key), range);
    }

    @Override
    public Set<String> rangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
        return this.operations.rangeByLex(getKey(key), range, limit);
    }
}
