package com.bestvike.serialization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2016/5/23.
 * Json 转换类
 * 字段上加注解控制行为 @JSONField
 */
public final class JsonSerializer {
    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化后的json
     */
    public static String serialize(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat);
    }

    /**
     * 反序列化
     *
     * @param json  字符串
     * @param clazz 类型
     * @return 对象
     */
    public static <T> T deserialize(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    /**
     * 反序列化
     *
     * @param json 字符串
     * @param type 类型引用
     * @param <T>
     * @return
     */
    public static <T> T deserialize(String json, TypeReference<T> type) {
        return JSON.parseObject(json, type);
    }

    /**
     * 反序列化数组
     *
     * @param json  字符串
     * @param clazz 类型
     * @return 对象列表
     */
    public static <T> List<T> deserializeArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    /**
     * 反序列化
     *
     * @param json 字符串
     * @return Map 实例
     */
    public static Map<String, Object> deserializeMap(String json) {
        return JSON.parseObject(json);
    }
}
