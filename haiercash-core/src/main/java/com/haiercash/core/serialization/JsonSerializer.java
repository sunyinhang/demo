package com.haiercash.core.serialization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.util.TypeUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2016/5/23.
 * Json 转换类
 * 字段上加注解控制行为 @JSONField
 */
public final class JsonSerializer {
    private static final FastJsonConfig GLOBAL_CONFIG;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    static {
        TypeUtils.compatibleWithJavaBean = true;
        TypeUtils.compatibleWithFieldName = false;
        GLOBAL_CONFIG = new FastJsonConfig();
        GLOBAL_CONFIG.setDateFormat(DATE_FORMAT);
        GLOBAL_CONFIG.setFeatures(
                Feature.AutoCloseSource,
                Feature.InternFieldNames,
                Feature.UseBigDecimal,
                Feature.AllowUnQuotedFieldNames,
                Feature.AllowSingleQuotes,
                Feature.AllowArbitraryCommas,
                Feature.SortFeidFastMatch,
                Feature.IgnoreNotMatch);
        GLOBAL_CONFIG.setSerializerFeatures(
                SerializerFeature.QuoteFieldNames,
                SerializerFeature.SkipTransientField,
                SerializerFeature.SortField,
                SerializerFeature.WriteEnumUsingName,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.IgnoreNonFieldGetter);
        GLOBAL_CONFIG.setSerializeFilters(new CommonValueFilter());
    }

    public static FastJsonConfig getGlobalConfig() {
        return GLOBAL_CONFIG;
    }

    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化后的json
     */
    public static String serialize(Object obj) {
        return JSON.toJSONString(obj,
                getGlobalConfig().getSerializeConfig(),
                getGlobalConfig().getSerializeFilters(),
                getGlobalConfig().getDateFormat(),
                Feature.of(getGlobalConfig().getFeatures()),
                getGlobalConfig().getSerializerFeatures());
    }

    /**
     * 反序列化
     *
     * @param json 字符串
     * @return 对象
     */
    public static Object deserialize(String json) {
        return JSON.parse(json);
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

    //序列化过滤器
    private static final class CommonValueFilter implements ValueFilter {
        @Override
        public Object process(Object object, String name, Object value) {
            if (value == null)
                return null;
            String type = value.getClass().getSimpleName();
            switch (type) {
                case "BigDecimal":
                    BigDecimal valueD = (BigDecimal) value;
                    if (valueD.compareTo(BigDecimal.valueOf(1.0E16D)) >= 0)
                        return String.valueOf(value);
                    break;
                case "Integer":
                    Integer valueN = (Integer) value;
                    if (valueN > 1000000000)
                        return String.valueOf(value);
                    break;
                case "Long":
                    Long valueL = (Long) value;
                    if (valueL > 1000000000L)
                        return String.valueOf(value);
                    break;
                default:
                    break;
            }
            return value;
        }
    }
}
