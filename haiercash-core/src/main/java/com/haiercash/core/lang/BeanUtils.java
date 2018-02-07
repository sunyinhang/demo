package com.haiercash.core.lang;

import com.alibaba.fastjson.TypeReference;
import com.haiercash.core.serialization.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * bean map 互相转换,依赖 fastjson
 *
 * @author 许崇雷
 * @date 2017/7/13
 */
public final class BeanUtils {
    /**
     * bean 转换为 map
     *
     * @param bean 对象
     * @return 字典
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null)
            return null;
        if (bean instanceof Map)
            //noinspection unchecked
            return (Map<String, Object>) bean;
        return JsonSerializer.deserializeMap(JsonSerializer.serialize(bean));
    }

    /**
     * map 转换为 bean
     *
     * @param map   字典
     * @param clazz 类型
     * @param <T>   类型
     * @return 对象
     */
    public static <T> T mapToBean(Map map, Class<T> clazz) {
        return mapToBean(map, (Type) clazz);
    }

    /**
     * map 转为 bean
     *
     * @param map  字典
     * @param type 类型引用
     * @param <T>  类型
     * @return 对象
     */
    public static <T> T mapToBean(Map map, TypeReference<T> type) {
        return mapToBean(map, type.getType());
    }

    /**
     * map 转为 bean
     *
     * @param map  字典
     * @param type 类型
     * @param <T>  类型
     * @return 对象
     */
    public static <T> T mapToBean(Map map, Type type) {
        return map == null ? null : JsonSerializer.deserialize(JsonSerializer.serialize(map), type);
    }
}
