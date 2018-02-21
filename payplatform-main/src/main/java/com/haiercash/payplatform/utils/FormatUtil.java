package com.haiercash.payplatform.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Format util.
 */
public class FormatUtil {
    private static final Log logger = LogFactory.getLog(FormatUtil.class);

    /**
     * 将一个map里的一些key-value挪至另一个map
     *
     * @param keys
     * @param outMap
     * @param inMap
     */
    public static void moveEntryBetweenMap(Stream<String> keys, Map<String, Object> outMap, Map<String, Object> inMap) {
        keys.filter(outMap::containsKey).forEach(key -> {
            inMap.put(key, outMap.get(key));
            outMap.remove(key);
        });
    }

    /**
     * 改变map中key的名字
     *
     * @param originKeys 原key值
     * @param changeKeys 改变后key值
     * @param map        需改变的map
     */
    public static void changeKeyName(List<String> originKeys, List<String> changeKeys, Map<String, Object> map) {
        if (map == null || map.isEmpty() || originKeys.isEmpty() || originKeys.size() != changeKeys.size()) {
            return;
        }
        for (int i = 0; i < originKeys.size(); i++) {
            if (map.containsKey(originKeys.get(i))) {
                Object value = map.get(originKeys.get(i));
                map.put(changeKeys.get(i), value);
                map.remove(originKeys.get(i));
            }
        }
    }

    public static Object checkValueType(String key, Object value, Class clazz) {
        try {
            Field field = clazz.getDeclaredField(key);
            if (value == null || "".equals(value)) {
                return value;
            }
            if (!field.getType().getTypeName().equals(value.getClass().getTypeName())) {
                if (String.class.getTypeName().equals(field.getType().getTypeName())) {
                    return String.valueOf(value);
                } else if (BigDecimal.class.getTypeName().equals(field.getType().getTypeName())) {
                    return new BigDecimal(String.valueOf(value));
                } else if (Double.class.getTypeName().equals(field.getType().getTypeName())) {
                    return Double.valueOf(value.toString());
                } else if (int.class.getTypeName().equals(field.getType().getTypeName())) {
                    return Integer.valueOf(value.toString());
                }
            }
        } catch (NoSuchFieldException e) {
            logger.info("反射获取apporder属性失败" + key);
            logger.error(String.valueOf(e));
        }
        return value;
    }
}
