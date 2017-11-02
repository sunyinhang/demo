package com.haiercash.payplatform.redis.core;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public class StringGeoOperations extends AbstractStringOperations implements GeoOperations<String, String> {
    private final GeoOperations<String, String> operations;

    public StringGeoOperations(RedisTemplate<String, String> template, GeoOperations<String, String> operations) {
        super(template);
        this.operations = operations;
    }

    @Override
    public Long geoAdd(String key, Point point, String member) {
        return this.operations.geoAdd(getKey(key), point, member);
    }

    @Override
    public Long geoAdd(String key, RedisGeoCommands.GeoLocation<String> location) {
        return this.operations.geoAdd(getKey(key), location);
    }

    @Override
    public Long geoAdd(String key, Map<String, Point> memberCoordinateMap) {
        return this.operations.geoAdd(getKey(key), memberCoordinateMap);
    }

    @Override
    public Long geoAdd(String key, Iterable<RedisGeoCommands.GeoLocation<String>> geoLocations) {
        return this.operations.geoAdd(getKey(key), geoLocations);
    }

    @Override
    public Distance geoDist(String key, String member1, String member2) {
        return this.operations.geoDist(getKey(key), member1, member2);
    }

    @Override
    public Distance geoDist(String key, String member1, String member2, Metric metric) {
        return this.operations.geoDist(getKey(key), member1, member2, metric);
    }

    @Override
    public List<String> geoHash(String key, String... members) {
        return this.operations.geoHash(getKey(key), members);
    }

    @Override
    public List<Point> geoPos(String key, String... members) {
        return this.operations.geoPos(getKey(key), members);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius(String key, Circle within) {
        return this.operations.geoRadius(getKey(key), within);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius(String key, Circle within, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return this.operations.geoRadius(getKey(key), within, args);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadiusByMember(String key, String member, double radius) {
        return this.operations.geoRadiusByMember(getKey(key), member, radius);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadiusByMember(String key, String member, Distance distance) {
        return this.operations.geoRadiusByMember(getKey(key), member, distance);
    }

    @Override
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadiusByMember(String key, String member, Distance distance, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return this.operations.geoRadiusByMember(getKey(key), member, distance, args);
    }

    @Override
    public Long geoRemove(String key, String... members) {
        return this.operations.geoRemove(getKey(key), members);
    }
}
