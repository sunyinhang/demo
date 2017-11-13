package com.haiercash.core.serialization;

import com.haiercash.core.collection.iterator.CharSequenceIterable;
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
    /**
     * Url 编码
     *
     * @param value
     * @param charsetName
     * @return
     */
    public static String encode(String value, String charsetName) {
        try {
            return URLEncoder.encode(value, charsetName);
        } catch (Exception e) {
            throw new RuntimeException("URL 编码失败,不支持的编码:" + charsetName, e);
        }
    }

    /**
     * Url 解码
     *
     * @param value
     * @param charsetName
     * @return
     */
    public static String decode(String value, String charsetName) {
        try {
            return URLDecoder.decode(value, charsetName);
        } catch (Exception e) {
            throw new RuntimeException("URL 解码失败,不支持的编码:" + charsetName, e);
        }
    }

    /**
     * Map 转 Url,Key 保留大小写
     *
     * @param map
     * @param charsetName
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> String mapToUrl(Map<K, V> map, String charsetName) {
        if (map == null)
            return null;
        StringBuilder builder = new StringBuilder();
        Map.Entry<K, V> current;
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        if (iterator.hasNext()) {
            current = iterator.next();
            builder.append(Convert.toString(current.getKey())).append("=").append(encode(Convert.toString(current.getValue()), charsetName));
            while (iterator.hasNext()) {
                current = iterator.next();
                builder.append("&").append(Convert.toString(current.getKey())).append("=").append(encode(Convert.toString(current.getValue()), charsetName));
            }
        }
        return builder.toString();
    }

    /**
     * Url 转 Map,Key 为小写,以便转换为bean
     *
     * @param url         url 字符串
     * @param charsetName 编码方式
     * @return
     */
    public static Map<String, String> urlToMap(String url, String charsetName) {
        return urlToMap(url, 0, charsetName);
    }

    /**
     * Url 转 Map,Key 为小写,以便转换为bean
     *
     * @param url         url 字符串
     * @param startIndex  起始索引,包含
     * @param charsetName 编码方式
     * @return
     */
    public static Map<String, String> urlToMap(String url, int startIndex, String charsetName) {
        return url == null ? null : urlToMap(url, startIndex, url.length(), charsetName);
    }

    /**
     * Url 转 Map,Key 为小写,以便转换为bean
     *
     * @param url         url 字符串
     * @param startIndex  起始索引,包含
     * @param endIndex    终止索引,不包含
     * @param charsetName 编码方式
     * @return
     */
    public static Map<String, String> urlToMap(String url, int startIndex, int endIndex, String charsetName) {
        if (url == null)
            return null;

        Map<String, String> map = new LinkedHashMap<>();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean iskey = true;
        for (char c : new CharSequenceIterable(url, startIndex, endIndex)) {
            switch (c) {
                case '=':
                    iskey = false;
                    break;
                case '&':
                    iskey = true;
                    //冲刷
                    if (key.length() > 0)
                        map.put(key.toString(), decode(value.toString(), charsetName));
                    key.setLength(0);
                    value.setLength(0);
                    break;
                default:
                    if (iskey)
                        key.append(c);
                    else
                        value.append(c);
                    break;
            }
        }
        //冲刷
        if (key.length() > 0)
            map.put(key.toString(), decode(value.toString(), charsetName));
        return map;
    }

    /**
     * 对象 转 url
     *
     * @param obj
     * @param charsetName
     * @return
     */
    public static String serialize(Object obj, String charsetName) {
        return obj == null ? null : mapToUrl(BeanUtils.beanToMap(obj), charsetName);
    }

    /**
     * url 转 对象
     *
     * @param url         url 字符串
     * @param charsetName 编码方式
     * @param clazz       类型
     * @param <T>         类型
     * @return
     */
    public static <T> T deserialize(String url, String charsetName, Class<T> clazz) {
        return deserialize(url, 0, charsetName, clazz);
    }

    /**
     * url 转 对象
     *
     * @param url         url 字符串
     * @param startIndex  起始索引,包含
     * @param charsetName 编码方式
     * @param clazz       类型
     * @param <T>         类型
     * @return
     */
    public static <T> T deserialize(String url, int startIndex, String charsetName, Class<T> clazz) {
        return url == null ? null : deserialize(url, startIndex, url.length(), charsetName, clazz);
    }

    /**
     * url 转 对象
     *
     * @param url         url 字符串
     * @param startIndex  起始索引,包含
     * @param endIndex    终止索引,不包含
     * @param charsetName 编码方式
     * @param clazz       类型
     * @param <T>         类型
     * @return
     */
    public static <T> T deserialize(String url, int startIndex, int endIndex, String charsetName, Class<T> clazz) {
        return BeanUtils.mapToBean(urlToMap(url, startIndex, endIndex, charsetName), clazz);
    }
}
