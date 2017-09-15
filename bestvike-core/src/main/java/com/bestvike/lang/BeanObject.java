package com.bestvike.lang;

import com.bestvike.serialization.JsonSerializer;

import java.util.Map;

/**
 * 表示 JavaBean 的基类,子类不要实现 Collection 接口
 *
 * @author 许崇雷
 * @date 2017/6/23
 */
public class BeanObject {
    /**
     * map 转换为 bean
     *
     * @param map   map 实例
     * @param clazz bean 类型
     * @param <T>   泛型
     * @return bean
     */
    public static <T extends BeanObject> T fromMap(Map map, Class<T> clazz) {
        return BeanUtils.mapToBean(map, clazz);
    }

    /**
     * json 字符串转换为 bean
     *
     * @param json  json 字符串
     * @param clazz bean 类型
     * @param <T>   泛型
     * @return bean
     */
    public static <T extends BeanObject> T fromJson(String json, Class<T> clazz) {
        return JsonSerializer.deserialize(json, clazz);
    }

    /**
     * 转换为 map
     *
     * @return map 实例
     */
    public Map<String, Object> toMap() {
        return BeanUtils.beanToMap(this);
    }

    /**
     * 转换为 json 字符串
     *
     * @return json 字符串
     */
    public String toJson() {
        return JsonSerializer.serialize(this);
    }

    /**
     * 转换为字符串
     *
     * @return json 字符串
     */
    @Override
    public String toString() {
        return this.toJson();
    }
}
