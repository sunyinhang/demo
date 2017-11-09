package com.haiercash.core.serialization;

import com.haiercash.core.collection.iterator.CharSequenceIterable;
import com.haiercash.core.lang.Convert;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2016/7/9.
 */
public final class MapSerializer {
    /**
     * 将Map转换为字符串
     *
     * @param map               Key,Value;Key,Value;Key,Value;...
     * @param keyValueSeparator , 分隔符
     * @param pairSeparator     ; 分隔符
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> String serialize(Map<K, V> map, char keyValueSeparator, char pairSeparator) {
        if (map == null)
            return null;

        StringBuilder builder = new StringBuilder();
        Map.Entry<K, V> current;
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        if (iterator.hasNext()) {
            current = iterator.next();
            builder.append(Convert.toString(current.getKey())).append(keyValueSeparator).append(Convert.toStringHuman(current.getValue()));
        }
        while (iterator.hasNext()) {
            current = iterator.next();
            builder.append(pairSeparator).append(Convert.toString(current.getKey())).append(keyValueSeparator).append(Convert.toStringHuman(current.getValue()));
        }
        return builder.toString();
    }

    /**
     * 将字符串分解为Map
     *
     * @param keyValueString    Key,Value;Key,Value;Key,Value;...
     * @param keyValueSeparator , 分隔符
     * @param pairSeparator     ; 分隔符
     * @return
     */
    public static Map<String, String> deserialize(String keyValueString, char keyValueSeparator, char pairSeparator) {
        return deserialize(keyValueString, 0, keyValueSeparator, pairSeparator);
    }

    /**
     * 将字符串分解为Map
     *
     * @param keyValueString    Key,Value;Key,Value;Key,Value;...
     * @param startIndex        起始索引,包含
     * @param keyValueSeparator , 分隔符
     * @param pairSeparator     ; 分隔符
     * @return
     */
    public static Map<String, String> deserialize(String keyValueString, int startIndex, char keyValueSeparator, char pairSeparator) {
        return keyValueString == null ? null : deserialize(keyValueString, startIndex, keyValueString.length(), keyValueSeparator, pairSeparator);
    }

    /**
     * 将字符串分解为Map
     *
     * @param keyValueString    Key,Value;Key,Value;Key,Value;...
     * @param startIndex        起始索引,包含
     * @param endIndex          终止索引,不包含
     * @param keyValueSeparator , 分隔符
     * @param pairSeparator     ; 分隔符
     * @return
     */
    public static Map<String, String> deserialize(String keyValueString, int startIndex, int endIndex, char keyValueSeparator, char pairSeparator) {
        if (keyValueString == null)
            return null;

        Map<String, String> map = new LinkedHashMap<>();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean iskey = true;
        for (char c : new CharSequenceIterable(keyValueString, startIndex, endIndex)) {
            if (Objects.equals(c, keyValueSeparator)) {
                iskey = false;
            } else if (Objects.equals(c, pairSeparator)) {
                iskey = true;
                //冲刷
                if (key.length() > 0)
                    map.put(key.toString(), value.toString());
                key.setLength(0);
                value.setLength(0);
            } else {
                if (iskey)
                    key.append(c);
                else
                    value.append(c);
            }
        }
        //冲刷
        if (key.length() > 0)
            map.put(key.toString(), value.toString());
        return map;
    }
}
