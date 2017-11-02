package com.haiercash.payplatform.redis.core;

import com.bestvike.linq.Linq;
import com.haiercash.payplatform.redis.RedisTemplateProvider;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-11-02.
 */
public abstract class AbstractStringOperations {
    private final RedisTemplate<String, String> template;

    protected AbstractStringOperations(RedisTemplate<String, String> template) {
        this.template = template;
    }

    protected static String getKey(String key) {
        return RedisTemplateProvider.getRedisConfigurationProperties().getKey(key);
    }

    protected static String[] getKeys(String[] keys) {
        return keys == null ? null : Linq.asEnumerable(keys).select(AbstractStringOperations::getKey).toArray(String.class);
    }

    protected static Collection<String> getKeys(Collection<String> keys) {
        return keys == null ? null : Linq.asEnumerable(keys).select(AbstractStringOperations::getKey).toList();
    }

    protected static <T> Map<? extends String, T> getKeyMap(Map<? extends String, T> map) {
        return map == null ? null : Linq.asEnumerable(map).toMap(entry -> getKey(entry.getKey()), Map.Entry::getValue);
    }

    public RedisOperations<String, String> getOperations() {
        return this.template;
    }
}
