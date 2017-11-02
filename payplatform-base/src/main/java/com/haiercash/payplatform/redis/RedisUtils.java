package com.haiercash.payplatform.redis;

import com.alibaba.fastjson.TypeReference;
import com.bestvike.serialization.JsonSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
public final class RedisUtils {
    private RedisUtils() {
    }

    //region 工具

    private static StringRedisTemplate getRedisTemplate() {
        return RedisTemplateProvider.getRedisTemplate();
    }

    private static RedisConfigurationProperties getRedisConfigurationProperties() {
        return RedisTemplateProvider.getRedisConfigurationProperties();
    }

    private static String serialize(Object value) {
        return JsonSerializer.serialize(value);
    }

    private static <T> T deserialize(String json, Class<T> clazz) {
        return JsonSerializer.deserialize(json, clazz);
    }

    private static <T> T deserialize(String json, TypeReference<T> type) {
        return JsonSerializer.deserialize(json, type);
    }

    //endregion


    //region 键命令

    public static void del(String key) {
        getRedisTemplate().delete(key);
    }

    public static void del(Collection<String> keys) {
        getRedisTemplate().delete(keys);
    }

    public static boolean exists(String key) {
        return getRedisTemplate().hasKey(key);
    }

    public static boolean expire(String key, long timeout, TimeUnit unit) {
        return getRedisTemplate().expire(key, timeout, unit);
    }

    public static boolean persist(String key) {
        return getRedisTemplate().persist(key);
    }

    public static void rename(String oldKey, String newKey) {
        getRedisTemplate().rename(oldKey, newKey);
    }

    public static boolean renamenx(String oldKey, String newKey) {
        return getRedisTemplate().renameIfAbsent(oldKey, newKey);
    }

    //endregion


    //region 字符串命令

    public static void set(String key, Object value) {
        if (getRedisConfigurationProperties().useValueExpire())
            getRedisTemplate().opsForValue().set(key, serialize(value), getRedisConfigurationProperties().getDefaultValueExpire(), getRedisConfigurationProperties().getTimeUnit());
        else
            getRedisTemplate().opsForValue().set(key, serialize(value));
    }

    public static <T> T get(String key, Class<T> clazz) {
        return deserialize(getRedisTemplate().opsForValue().get(key), clazz);
    }

    public static <T> T get(String key, TypeReference<T> type) {
        return deserialize(getRedisTemplate().opsForValue().get(key), type);
    }

    public static String getString(String key) {
        return get(key, String.class);
    }

    public static Map<String, Object> getMap(String key) {
        return get(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static boolean setnx(String key, Object value) {
        return getRedisTemplate().opsForValue().setIfAbsent(key, serialize(value));
    }

    //endregion


    //region 哈希命令

    public static long hdel(String key, String field) {
        return getRedisTemplate().opsForHash().delete(key, field);
    }

    public static boolean hexists(String key, String field) {
        return getRedisTemplate().opsForHash().hasKey(key, field);
    }

    public static void hset(String key, String field, Object value) {
        getRedisTemplate().opsForHash().put(key, field, serialize(value));
    }

    public static <T> T hget(String key, String field, Class<T> clazz) {
        return deserialize(getRedisTemplate().<String, String>opsForHash().get(key, field), clazz);
    }

    public static <T> T hget(String key, String field, TypeReference<T> type) {
        return deserialize(getRedisTemplate().<String, String>opsForHash().get(key, field), type);
    }

    public static String hgetString(String key, String field) {
        return hget(key, field, String.class);
    }

    public static Map<String, Object> hgetMap(String key, String field) {
        return hget(key, field, new TypeReference<Map<String, Object>>() {
        });
    }

    public static void hsetnx(String key, String field, Object value) {
        getRedisTemplate().opsForHash().putIfAbsent(key, field, serialize(value));
    }

    //endregion


    //region 列表操作

    public static <T> T blpop(String key, long timeout, TimeUnit unit, Class<T> clazz) {
        return deserialize(getRedisTemplate().opsForList().leftPop(key, timeout, unit), clazz);
    }

    public static <T> T blpop(String key, long timeout, TimeUnit unit, TypeReference<T> type) {
        return deserialize(getRedisTemplate().opsForList().leftPop(key, timeout, unit), type);
    }

    public static String blpopString(String key, long timeout, TimeUnit unit) {
        return blpop(key, timeout, unit, String.class);
    }

    public static Map<String, Object> blpopMap(String key, long timeout, TimeUnit unit) {
        return blpop(key, timeout, unit, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T brpop(String key, long timeout, TimeUnit unit, Class<T> clazz) {
        return deserialize(getRedisTemplate().opsForList().rightPop(key, timeout, unit), clazz);
    }

    public static <T> T brpop(String key, long timeout, TimeUnit unit, TypeReference<T> type) {
        return deserialize(getRedisTemplate().opsForList().rightPop(key, timeout, unit), type);
    }

    public static String brpopString(String key, long timeout, TimeUnit unit) {
        return brpop(key, timeout, unit, String.class);
    }

    public static Map<String, Object> brpopMap(String key, long timeout, TimeUnit unit) {
        return brpop(key, timeout, unit, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T lpop(String key, Class<T> clazz) {
        return deserialize(getRedisTemplate().opsForList().leftPop(key), clazz);
    }

    public static <T> T lpop(String key, TypeReference<T> type) {
        return deserialize(getRedisTemplate().opsForList().leftPop(key), type);
    }

    public static String lpopString(String key) {
        return lpop(key, String.class);
    }

    public static Map<String, Object> lpopMap(String key) {
        return lpop(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static void lpush(String key, Object value) {
        getRedisTemplate().opsForList().leftPush(key, serialize(value));
    }

    public static void lpushx(String key, Object value) {
        getRedisTemplate().opsForList().leftPushIfPresent(key, serialize(value));
    }

    public static void lpushx(String key, long count, Object value) {
        getRedisTemplate().opsForList().remove(key, count, serialize(value));
    }

    public static <T> T rpop(String key, Class<T> clazz) {
        return deserialize(getRedisTemplate().opsForList().rightPop(key), clazz);
    }

    public static <T> T rpop(String key, TypeReference<T> type) {
        return deserialize(getRedisTemplate().opsForList().rightPop(key), type);
    }

    public static String rpopString(String key) {
        return rpop(key, String.class);
    }

    public static Map<String, Object> rpopMap(String key) {
        return rpop(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static void rpush(String key, Object value) {
        getRedisTemplate().opsForList().rightPush(key, serialize(value));
    }

    public static void rpushx(String key, Object value) {
        getRedisTemplate().opsForList().rightPushIfPresent(key, serialize(value));
    }

    //endregion
}
