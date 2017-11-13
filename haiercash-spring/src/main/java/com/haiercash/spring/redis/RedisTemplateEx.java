package com.haiercash.spring.redis;

import com.haiercash.spring.redis.core.StringGeoOperations;
import com.haiercash.spring.redis.core.StringHashOperations;
import com.haiercash.spring.redis.core.StringHyperLogLogOperations;
import com.haiercash.spring.redis.core.StringListOperations;
import com.haiercash.spring.redis.core.StringSetOperations;
import com.haiercash.spring.redis.core.StringValueOperations;
import com.haiercash.spring.redis.core.StringZSetOperations;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
public final class RedisTemplateEx extends StringRedisTemplate {
    // properties
    RedisProperties properties;
    // cache singleton objects (where possible)
    private ValueOperations<String, String> valueOps;
    private ListOperations<String, String> listOps;
    private SetOperations<String, String> setOps;
    private ZSetOperations<String, String> zSetOps;
    private GeoOperations<String, String> geoOps;
    private HyperLogLogOperations<String, String> hllOps;

    public RedisTemplateEx(RedisConnectionFactory connectionFactory) {
        super(connectionFactory);
        //must set properties after construct
    }

    @Override
    public ValueOperations<String, String> opsForValue() {
        if (this.valueOps == null)
            this.valueOps = new StringValueOperations(this, this.properties, super.opsForValue());
        return this.valueOps;
    }

    @Override
    public ListOperations<String, String> opsForList() {
        if (this.listOps == null)
            this.listOps = new StringListOperations(this, this.properties, super.opsForList());
        return this.listOps;
    }

    @Override
    public SetOperations<String, String> opsForSet() {
        if (this.setOps == null)
            this.setOps = new StringSetOperations(this, this.properties, super.opsForSet());
        return this.setOps;
    }

    @Override
    public ZSetOperations<String, String> opsForZSet() {
        if (this.zSetOps == null)
            this.zSetOps = new StringZSetOperations(this, this.properties, super.opsForZSet());
        return this.zSetOps;
    }

    @Override
    public GeoOperations<String, String> opsForGeo() {
        if (this.geoOps == null)
            this.geoOps = new StringGeoOperations(this, this.properties, super.opsForGeo());
        return this.geoOps;
    }

    @Override
    public HyperLogLogOperations<String, String> opsForHyperLogLog() {
        if (this.hllOps == null)
            this.hllOps = new StringHyperLogLogOperations(this, this.properties, super.opsForHyperLogLog());
        return this.hllOps;
    }

    @Override
    public <HK, HV> HashOperations<String, HK, HV> opsForHash() {
        return new StringHashOperations<>(this, this.properties, super.opsForHash());
    }

    //region 键命令

    @Override
    public void delete(String key) {
        super.delete(this.properties.getKey(key));
    }

    @Override
    public void delete(Collection<String> keys) {
        super.delete(this.properties.getKeys(keys));
    }

    @Override
    public Boolean hasKey(String key) {
        return super.hasKey(this.properties.getKey(key));
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return super.expire(this.properties.getKey(key), timeout, unit);
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        return super.expireAt(this.properties.getKey(key), date);
    }

    @Override
    public Long getExpire(String key) {
        return super.getExpire(this.properties.getKey(key));
    }

    @Override
    public Long getExpire(String key, TimeUnit timeUnit) {
        return super.getExpire(this.properties.getKey(key), timeUnit);
    }

    @Override
    public Boolean persist(String key) {
        return super.persist(this.properties.getKey(key));
    }

    @Override
    public Boolean move(String key, int dbIndex) {
        return super.move(this.properties.getKey(key), dbIndex);
    }

    @Override
    public void rename(String oldKey, String newKey) {
        super.rename(this.properties.getKey(oldKey), this.properties.getKey(newKey));
    }

    @Override
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return super.renameIfAbsent(this.properties.getKey(oldKey), this.properties.getKey(newKey));
    }

    @Override
    public DataType type(String key) {
        return super.type(this.properties.getKey(key));
    }

    @Override
    public byte[] dump(String key) {
        return super.dump(this.properties.getKey(key));
    }

    @Override
    public void restore(String key, byte[] value, long timeToLive, TimeUnit unit) {
        super.restore(this.properties.getKey(key), value, timeToLive, unit);
    }

    @Override
    public void watch(String key) {
        super.watch(this.properties.getKey(key));
    }

    @Override
    public void watch(Collection<String> keys) {
        super.watch(this.properties.getKeys(keys));
    }

    //endregion
}
