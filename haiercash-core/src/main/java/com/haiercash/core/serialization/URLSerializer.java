package com.haiercash.core.serialization;

import com.haiercash.core.collection.iterator.CharSequenceIterable;
import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2016/6/15.
 */
public final class URLSerializer {
    private static final String DEFAULT_CHARSET = CharsetNames.UTF_8;

    /**
     * Url 编码
     *
     * @param value
     * @return
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, DEFAULT_CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("URL 编码失败,不支持的编码:" + DEFAULT_CHARSET, e);
        }
    }

    /**
     * Url 解码
     *
     * @param value
     * @return
     */
    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, DEFAULT_CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("URL 解码失败,不支持的编码:" + DEFAULT_CHARSET, e);
        }
    }

    /**
     * Map 转 Url,Key 保留大小写
     *
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> String mapToUrl(Map<K, V> map) {
        if (map == null)
            return null;
        StringBuilder builder = new StringBuilder();
        Map.Entry<K, V> current;
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        if (iterator.hasNext()) {
            current = iterator.next();
            builder.append(Convert.toString(current.getKey())).append("=").append(encode(Convert.toStringHuman(current.getValue())));
            while (iterator.hasNext()) {
                current = iterator.next();
                builder.append("&").append(Convert.toString(current.getKey())).append("=").append(encode(Convert.toStringHuman(current.getValue())));
            }
        }
        return builder.toString();
    }

    /**
     * Url 转 Map,Key 为小写,以便转换为bean
     *
     * @param url url 字符串
     * @return
     */
    public static Map<String, String> urlToMap(String url) {
        return urlToMap(url, 0);
    }

    /**
     * Url 转 Map,Key 为小写,以便转换为bean
     *
     * @param url        url 字符串
     * @param startIndex 起始索引,包含
     * @return
     */
    public static Map<String, String> urlToMap(String url, int startIndex) {
        return url == null ? null : urlToMap(url, startIndex, url.length());
    }

    /**
     * Url 转 Map,Key 为小写,以便转换为bean
     *
     * @param url        url 字符串
     * @param startIndex 起始索引,包含
     * @param endIndex   终止索引,不包含
     * @return
     */
    public static Map<String, String> urlToMap(String url, int startIndex, int endIndex) {
        if (url == null)
            return null;

        Map<String, String> map = new LinkedHashMap<>();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean isKey = true;
        for (char c : new CharSequenceIterable(url, startIndex, endIndex)) {
            switch (c) {
                case '=':
                    isKey = false;
                    break;
                case '&':
                    isKey = true;
                    //冲刷
                    if (key.length() > 0)
                        map.put(key.toString(), decode(value.toString()));
                    key.setLength(0);
                    value.setLength(0);
                    break;
                default:
                    if (isKey)
                        key.append(c);
                    else
                        value.append(c);
                    break;
            }
        }
        //冲刷
        if (key.length() > 0)
            map.put(key.toString(), decode(value.toString()));
        return map;
    }

    /**
     * 对象 转 url
     *
     * @param obj
     * @return
     */
    public static String serialize(Object obj) {
        return obj == null ? null : mapToUrl(BeanUtils.beanToMap(obj));
    }

    /**
     * url 转 对象
     *
     * @param url   url 字符串
     * @param clazz 类型
     * @param <T>   类型
     * @return
     */
    public static <T> T deserialize(String url, Class<T> clazz) {
        return deserialize(url, 0, clazz);
    }

    /**
     * url 转 对象
     *
     * @param url        url 字符串
     * @param startIndex 起始索引,包含
     * @param clazz      类型
     * @param <T>        类型
     * @return
     */
    public static <T> T deserialize(String url, int startIndex, Class<T> clazz) {
        return url == null ? null : deserialize(url, startIndex, url.length(), clazz);
    }

    /**
     * url 转 对象
     *
     * @param url        url 字符串
     * @param startIndex 起始索引,包含
     * @param endIndex   终止索引,不包含
     * @param clazz      类型
     * @param <T>        类型
     * @return
     */
    public static <T> T deserialize(String url, int startIndex, int endIndex, Class<T> clazz) {
        return BeanUtils.mapToBean(urlToMap(url, startIndex, endIndex), clazz);
    }
}
