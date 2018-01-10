package com.haiercash.spring.redis.converter;

import com.alibaba.fastjson.TypeReference;
import com.haiercash.core.serialization.JsonSerializer;

/**
 * Created by 许崇雷 on 2017-12-07.
 */
public final class FastJsonRedisSerializer {
    public String serialize(Object value) {
        return JsonSerializer.serialize(value);
    }

    public Object deserialize(String json) {
        return JsonSerializer.deserialize(json);
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        return JsonSerializer.deserialize(json, clazz);
    }

    public <T> T deserialize(String json, TypeReference<T> type) {
        return JsonSerializer.deserialize(json, type);
    }
}
