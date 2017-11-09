package com.haiercash.core.lang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.haiercash.core.serialization.JsonSerializer;

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
    @SuppressWarnings("unchecked")
    public static Map<String, Object> beanToMap(Object bean) {
        return bean instanceof Map ? (Map<String, Object>) bean : (JSONObject) JSON.toJSON(bean, JsonSerializer.getGlobalConfig().getSerializeConfig());
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
        return TypeUtils.castToJavaBean(map, clazz);
    }
}
