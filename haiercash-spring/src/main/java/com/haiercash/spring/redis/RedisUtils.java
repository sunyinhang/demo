package com.haiercash.spring.redis;

import com.alibaba.fastjson.TypeReference;
import com.bestvike.linq.Linq;
import com.haiercash.spring.redis.converter.FastJsonRedisSerializer;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
public final class RedisUtils {
    private static final FastJsonRedisSerializer SERIALIZER = new FastJsonRedisSerializer();

    private RedisUtils() {
    }

    //region 工具

    private static RedisTemplateEx getRedisTemplate() {
        return RedisTemplateProvider.getRedisTemplate();
    }

    private static RedisProperties getRedisProperties() {
        return getRedisTemplate().getProperties();
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


    //region 字符串命令-常用

    public static void setExpire(String key, Object value) {
        if (getRedisProperties().valueExpireEnabled())
            getRedisTemplate().opsForValue().set(key, SERIALIZER.serialize(value), getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
        else
            getRedisTemplate().opsForValue().set(key, SERIALIZER.serialize(value));
    }

    @SuppressWarnings("Duplicates")
    public static <T> T getExpire(String key, Class<T> clazz) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundValueOperations<String, String> operations = getRedisTemplate().boundValueOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return SERIALIZER.deserialize(operations.get(), clazz);
        } else {
            return SERIALIZER.deserialize(getRedisTemplate().opsForValue().get(key), clazz);
        }
    }

    @SuppressWarnings("Duplicates")
    public static <T> T getExpire(String key, TypeReference<T> type) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundValueOperations<String, String> operations = getRedisTemplate().boundValueOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return SERIALIZER.deserialize(operations.get(), type);
        } else {
            return SERIALIZER.deserialize(getRedisTemplate().opsForValue().get(key), type);
        }
    }

    public static String getExpireString(String key) {
        return getExpire(key, String.class);
    }

    public static Map<String, Object> getExpireMap(String key) {
        return getExpire(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static boolean setnxExpire(String key, Object value) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundValueOperations<String, String> operations = getRedisTemplate().boundValueOps(key);
            boolean success = operations.setIfAbsent(SERIALIZER.serialize(value));
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return success;
        } else {
            return getRedisTemplate().opsForValue().setIfAbsent(key, SERIALIZER.serialize(value));
        }
    }

    @SuppressWarnings("Duplicates")
    public static <T> T getSetExpire(String key, Object value, Class<T> clazz) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundValueOperations<String, String> operations = getRedisTemplate().boundValueOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return SERIALIZER.deserialize(operations.getAndSet(SERIALIZER.serialize(value)), clazz);
        } else {
            return SERIALIZER.deserialize(getRedisTemplate().opsForValue().getAndSet(key, SERIALIZER.serialize(value)), clazz);
        }
    }

    @SuppressWarnings("Duplicates")
    public static <T> T getSetExpire(String key, Object value, TypeReference<T> type) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundValueOperations<String, String> operations = getRedisTemplate().boundValueOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return SERIALIZER.deserialize(operations.getAndSet(SERIALIZER.serialize(value)), type);
        } else {
            return SERIALIZER.deserialize(getRedisTemplate().opsForValue().getAndSet(key, SERIALIZER.serialize(value)), type);
        }
    }

    public static String getSetExpireString(String key, Object value) {
        return getSetExpire(key, value, String.class);
    }

    public static Map<String, Object> getSetExpireMap(String key, Object value) {
        return getSetExpire(key, value, new TypeReference<Map<String, Object>>() {
        });
    }

    //endregion


    //region 字符串命令

    public static void set(String key, Object value) {
        getRedisTemplate().opsForValue().set(key, SERIALIZER.serialize(value));
    }

    public static void set(String key, Object value, long timeout, TimeUnit unit) {
        getRedisTemplate().opsForValue().set(key, SERIALIZER.serialize(value), timeout, unit);
    }

    public static <T> T get(String key, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForValue().get(key), clazz);
    }

    public static <T> T get(String key, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForValue().get(key), type);
    }

    public static String getString(String key) {
        return get(key, String.class);
    }

    public static Map<String, Object> getMap(String key) {
        return get(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static boolean setnx(String key, Object value) {
        return getRedisTemplate().opsForValue().setIfAbsent(key, SERIALIZER.serialize(value));
    }

    public static <T> T getSet(String key, Object value, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForValue().getAndSet(key, SERIALIZER.serialize(value)), clazz);
    }

    public static <T> T getSet(String key, Object value, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForValue().getAndSet(key, SERIALIZER.serialize(value)), type);
    }

    public static String getSetString(String key, Object value) {
        return getSet(key, value, String.class);
    }

    public static Map<String, Object> getSetMap(String key, Object value) {
        return getSet(key, value, new TypeReference<Map<String, Object>>() {
        });
    }

    //endregion


    //region 哈希命令-常用

    public static void hsetExpire(String key, String field, Object value) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundHashOperations<String, String, String> operations = getRedisTemplate().boundHashOps(key);
            operations.put(field, SERIALIZER.serialize(value));
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
        } else {
            getRedisTemplate().<String, String>opsForHash().put(key, field, SERIALIZER.serialize(value));
        }
    }

    @SuppressWarnings("Duplicates")
    public static <T> T hgetExpire(String key, String field, Class<T> clazz) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundHashOperations<String, String, String> operations = getRedisTemplate().boundHashOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return SERIALIZER.deserialize(operations.get(field), clazz);
        } else {
            return SERIALIZER.deserialize(getRedisTemplate().<String, String>opsForHash().get(key, field), clazz);
        }
    }

    @SuppressWarnings("Duplicates")
    public static <T> T hgetExpire(String key, String field, TypeReference<T> type) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundHashOperations<String, String, String> operations = getRedisTemplate().boundHashOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return SERIALIZER.deserialize(operations.get(field), type);
        } else {
            return SERIALIZER.deserialize(getRedisTemplate().<String, String>opsForHash().get(key, field), type);
        }
    }

    public static String hgetExpireString(String key, String field) {
        return hgetExpire(key, field, String.class);
    }

    public static Map<String, Object> hgetExpireMap(String key, String field) {
        return hgetExpire(key, field, new TypeReference<Map<String, Object>>() {
        });
    }

    public static boolean hsetnxExpire(String key, String field, Object value) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundHashOperations<String, String, String> operations = getRedisTemplate().boundHashOps(key);
            boolean success = operations.putIfAbsent(field, SERIALIZER.serialize(value));
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            return success;
        } else {
            return getRedisTemplate().<String, String>opsForHash().putIfAbsent(key, field, SERIALIZER.serialize(value));
        }
    }

    public static void hmsetExpire(String key, Map<String, Object> map) {
        Map<String, String> stringMap = Linq.asEnumerable(map).toMap(Map.Entry::getKey, entry -> SERIALIZER.serialize(entry.getValue()));
        if (getRedisProperties().valueExpireEnabled()) {
            BoundHashOperations<String, String, String> operations = getRedisTemplate().boundHashOps(key);
            operations.putAll(stringMap);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
        } else {
            getRedisTemplate().<String, String>opsForHash().putAll(key, stringMap);
        }
    }

    public static Map<String, Object> hgetAllExpire(String key) {
        if (getRedisProperties().valueExpireEnabled()) {
            BoundHashOperations<String, String, String> operations = getRedisTemplate().boundHashOps(key);
            operations.expire(getRedisProperties().getDefaultValueExpire(), getRedisProperties().getTimeUnit());
            Map<String, String> stringMap = operations.entries();
            return Linq.asEnumerable(stringMap).toMap(Map.Entry::getKey, entry -> SERIALIZER.deserialize(entry.getValue()));
        } else {
            Map<String, String> stringMap = getRedisTemplate().<String, String>opsForHash().entries(key);
            return Linq.asEnumerable(stringMap).toMap(Map.Entry::getKey, entry -> SERIALIZER.deserialize(entry.getValue()));
        }
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
        getRedisTemplate().opsForHash().put(key, field, SERIALIZER.serialize(value));
    }

    public static <T> T hget(String key, String field, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().<String, String>opsForHash().get(key, field), clazz);
    }

    public static <T> T hget(String key, String field, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().<String, String>opsForHash().get(key, field), type);
    }

    public static String hgetString(String key, String field) {
        return hget(key, field, String.class);
    }

    public static Map<String, Object> hgetMap(String key, String field) {
        return hget(key, field, new TypeReference<Map<String, Object>>() {
        });
    }

    public static boolean hsetnx(String key, String field, Object value) {
        return getRedisTemplate().opsForHash().putIfAbsent(key, field, SERIALIZER.serialize(value));
    }

    public static void hmset(String key, Map<String, Object> map) {
        Map<String, String> stringMap = Linq.asEnumerable(map).toMap(Map.Entry::getKey, entry -> SERIALIZER.serialize(entry.getValue()));
        getRedisTemplate().opsForHash().putAll(key, stringMap);
    }

    public static Map<String, Object> hgetAll(String key) {
        Map<String, String> stringMap = getRedisTemplate().<String, String>opsForHash().entries(key);
        return Linq.asEnumerable(stringMap).toMap(Map.Entry::getKey, entry -> SERIALIZER.deserialize(entry.getValue()));
    }

    //endregion


    //region 列表操作

    public static long lrem(String key, long count, Object value) {
        return getRedisTemplate().opsForList().remove(key, count, SERIALIZER.serialize(value));
    }

    public static List<String> lrange(String key, long start, long end) {
        return getRedisTemplate().opsForList().range(key, start, end);
    }

    public static <T> T blpop(String key, long timeout, TimeUnit unit, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().leftPop(key, timeout, unit), clazz);
    }

    public static <T> T blpop(String key, long timeout, TimeUnit unit, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().leftPop(key, timeout, unit), type);
    }

    public static String blpopString(String key, long timeout, TimeUnit unit) {
        return blpop(key, timeout, unit, String.class);
    }

    public static Map<String, Object> blpopMap(String key, long timeout, TimeUnit unit) {
        return blpop(key, timeout, unit, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T brpop(String key, long timeout, TimeUnit unit, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().rightPop(key, timeout, unit), clazz);
    }

    public static <T> T brpop(String key, long timeout, TimeUnit unit, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().rightPop(key, timeout, unit), type);
    }

    public static String brpopString(String key, long timeout, TimeUnit unit) {
        return brpop(key, timeout, unit, String.class);
    }

    public static Map<String, Object> brpopMap(String key, long timeout, TimeUnit unit) {
        return brpop(key, timeout, unit, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T lpop(String key, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().leftPop(key), clazz);
    }

    public static <T> T lpop(String key, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().leftPop(key), type);
    }

    public static String lpopString(String key) {
        return lpop(key, String.class);
    }

    public static Map<String, Object> lpopMap(String key) {
        return lpop(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static long lpush(String key, Object value) {
        return getRedisTemplate().opsForList().leftPush(key, SERIALIZER.serialize(value));
    }

    public static long lpushx(String key, Object value) {
        return getRedisTemplate().opsForList().leftPushIfPresent(key, SERIALIZER.serialize(value));
    }

    public static <T> T rpop(String key, Class<T> clazz) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().rightPop(key), clazz);
    }

    public static <T> T rpop(String key, TypeReference<T> type) {
        return SERIALIZER.deserialize(getRedisTemplate().opsForList().rightPop(key), type);
    }

    public static String rpopString(String key) {
        return rpop(key, String.class);
    }

    public static Map<String, Object> rpopMap(String key) {
        return rpop(key, new TypeReference<Map<String, Object>>() {
        });
    }

    public static long rpush(String key, Object value) {
        return getRedisTemplate().opsForList().rightPush(key, SERIALIZER.serialize(value));
    }

    public static long rpushx(String key, Object value) {
        return getRedisTemplate().opsForList().rightPushIfPresent(key, SERIALIZER.serialize(value));
    }

    //endregion
}
