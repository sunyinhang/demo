package com.haiercash.payplatform.redis;

import com.bestvike.linq.Linq;
import com.haiercash.payplatform.redis.core.StringGeoOperations;
import com.haiercash.payplatform.redis.core.StringHashOperations;
import com.haiercash.payplatform.redis.core.StringHyperLogLogOperations;
import com.haiercash.payplatform.redis.core.StringListOperations;
import com.haiercash.payplatform.redis.core.StringSetOperations;
import com.haiercash.payplatform.redis.core.StringValueOperations;
import com.haiercash.payplatform.redis.core.StringZSetOperations;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundGeoOperations;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
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
    // cache singleton objects (where possible)
    private ValueOperations<String, String> valueOps;
    private ListOperations<String, String> listOps;
    private SetOperations<String, String> setOps;
    private ZSetOperations<String, String> zSetOps;
    private GeoOperations<String, String> geoOps;
    private HyperLogLogOperations<String, String> hllOps;

    public RedisTemplateEx(RedisConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    private static String getKey(String key) {
        return RedisTemplateProvider.getRedisProperties().getKey(key);
    }

    private static Collection<String> getKeys(Collection<String> keys) {
        return keys == null ? null : Linq.asEnumerable(keys).select(RedisTemplateEx::getKey).toList();
    }

    @Override
    public ValueOperations<String, String> opsForValue() {
        if (this.valueOps == null)
            this.valueOps = new StringValueOperations(this, super.opsForValue());
        return this.valueOps;
    }

    @Override
    public ListOperations<String, String> opsForList() {
        if (this.listOps == null)
            this.listOps = new StringListOperations(this, super.opsForList());
        return this.listOps;
    }

    @Override
    public SetOperations<String, String> opsForSet() {
        if (this.setOps == null)
            this.setOps = new StringSetOperations(this, super.opsForSet());
        return this.setOps;
    }

    @Override
    public ZSetOperations<String, String> opsForZSet() {
        if (this.zSetOps == null)
            this.zSetOps = new StringZSetOperations(this, super.opsForZSet());
        return this.zSetOps;
    }

    @Override
    public GeoOperations<String, String> opsForGeo() {
        if (this.geoOps == null)
            this.geoOps = new StringGeoOperations(this, super.opsForGeo());
        return this.geoOps;
    }

    @Override
    public HyperLogLogOperations<String, String> opsForHyperLogLog() {
        if (this.hllOps == null)
            this.hllOps = new StringHyperLogLogOperations(this, super.opsForHyperLogLog());
        return this.hllOps;
    }

    @Override
    public <HK, HV> HashOperations<String, HK, HV> opsForHash() {
        return new StringHashOperations<>(this, super.opsForHash());
    }

    //region 键命令

    @Override
    public void delete(String key) {
        super.delete(getKey(key));
    }

    @Override
    public void delete(Collection<String> keys) {
        super.delete(getKeys(keys));
    }

    @Override
    public Boolean hasKey(String key) {
        return super.hasKey(getKey(key));
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return super.expire(getKey(key), timeout, unit);
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        return super.expireAt(getKey(key), date);
    }

    @Override
    public Long getExpire(String key) {
        return super.getExpire(getKey(key));
    }

    @Override
    public Long getExpire(String key, TimeUnit timeUnit) {
        return super.getExpire(getKey(key), timeUnit);
    }

    @Override
    public Boolean persist(String key) {
        return super.persist(getKey(key));
    }

    @Override
    public Boolean move(String key, int dbIndex) {
        return super.move(getKey(key), dbIndex);
    }

    @Override
    public void rename(String oldKey, String newKey) {
        super.rename(getKey(oldKey), getKey(newKey));
    }

    @Override
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return super.renameIfAbsent(getKey(oldKey), getKey(newKey));
    }

    @Override
    public DataType type(String key) {
        return super.type(getKey(key));
    }

    @Override
    public byte[] dump(String key) {
        return super.dump(getKey(key));
    }

    @Override
    public void restore(String key, byte[] value, long timeToLive, TimeUnit unit) {
        super.restore(getKey(key), value, timeToLive, unit);
    }

    @Override
    public void watch(String key) {
        super.watch(getKey(key));
    }

    @Override
    public void watch(Collection<String> keys) {
        super.watch(getKeys(keys));
    }


    @Override
    public BoundValueOperations<String, String> boundValueOps(String key) {
        return super.boundValueOps(getKey(key));
    }

    @Override
    public BoundListOperations<String, String> boundListOps(String key) {
        return super.boundListOps(getKey(key));
    }

    @Override
    public BoundSetOperations<String, String> boundSetOps(String key) {
        return super.boundSetOps(getKey(key));
    }

    @Override
    public BoundZSetOperations<String, String> boundZSetOps(String key) {
        return super.boundZSetOps(getKey(key));
    }

    @Override
    public BoundGeoOperations<String, String> boundGeoOps(String key) {
        return super.boundGeoOps(getKey(key));
    }

    @Override
    public <HK, HV> BoundHashOperations<String, HK, HV> boundHashOps(String key) {
        return super.boundHashOps(getKey(key));
    }

    //endregion
}
