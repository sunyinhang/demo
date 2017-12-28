package com.haiercash.payplatform.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Format util.
 */
public class FormatUtil {

    /**
     * global logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(FormatUtil.class);

    /**
     * reuse ObjectMapper.
     */
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    static {
        jsonMapper.setSerializationInclusion(Include.NON_EMPTY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMapper.setDateFormat(sdf);
        DeserializationConfig deCfg = jsonMapper.getDeserializationConfig();
        deCfg.withFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    /**
     * 将json格式字符串转换为List列表.
     *
     * @param <E>        Object type.
     * @param jsonString string to format.
     * @param clazz      要转换的对象class
     * @return a list of objects.
     */
    public static <E> List<E> toObjectList(String jsonString, Class<E> clazz) {

        List<E> list = new ArrayList<>();
        try {
            TypeFactory typeFactory = jsonMapper.getTypeFactory();
            list = jsonMapper.readValue(jsonString, typeFactory.constructCollectionType(List.class, clazz));
        } catch (JsonParseException e) {
            logger.error("fail to parse json data" + jsonString);
            logger.debug("Error:", e);
        } catch (IOException e) {
            logger.error("fail to read/write json data" + jsonString);
            logger.debug("Error:", e);
        }
        return list;
    }

    /**
     * @param jsonString jsonString
     * @return map list
     * @since v5.0
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<Map> toMapList(String jsonString) {
        List<Map> list = new ArrayList<>();
        try {
            list = jsonMapper.readValue(jsonString, List.class);
        } catch (JsonParseException e) {
            logger.error("fail to parse json data" + jsonString);
            logger.error("Error:", e);
        } catch (IOException e) {
            logger.error("fail to read/write json data" + jsonString);
            logger.error("Error:", e);
        }
        return list;
    }

    /**
     * 将json格式字符串转换为对象E.
     *
     * @param <E>        type of object.
     * @param jsonString string of json type
     * @param clazz      要转换的对象class
     * @return object
     */
    public static <E> E toObject(String jsonString, Class<E> clazz) {

        E result = null;
        try {
            result = jsonMapper.readValue(jsonString, clazz);
        } catch (JsonParseException e) {
            logger.error("fail to parse json data" + jsonString);
            logger.error("Error:", e);
        } catch (JsonMappingException e) {
            logger.error("fail to mapping json data" + jsonString);
            logger.error("Error:", e);
        } catch (IOException e) {
            logger.error("fail to read/write json data" + jsonString);
            logger.error("Error:", e);
        }

        return result;
    }

    /**
     * 将Map转换为Object，目前仅支持Map<String, String>.
     *
     * @param <T>   结果泛型
     * @param map   源数据
     * @param clazz 转换后的类
     * @return 转换后的对象
     */
    public static <T> T toObject(Map<String, String> map, Class<T> clazz) {
        try {
            if (map == null || map.isEmpty()) {
                return null;
            }
            // 借助jsonMapper进行二次转换
            String json = jsonMapper.writeValueAsString(map);
            return jsonMapper.readValue(json, clazz);
        } catch (JsonParseException e) {
            logger.error("fail to parse data");
            logger.error("Error:", e);
        } catch (JsonMappingException e) {
            logger.error("fail to mapping data");
            logger.error("Error:", e);
        } catch (IOException e) {
            logger.error("fail to read/write data");
            logger.error("Error:", e);
        }
        return null;
    }

    /**
     * transform object to json string.
     *
     * @param obj object to transform
     * @return json string
     */
    public static String toJson(Object obj) {
        try {
            return jsonMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize an object from an XML String.
     *
     * @param xml   xml str
     * @param types the classes with XStream annotations to convert to.
     * @return the object deserialized
     */
    @SuppressWarnings("rawtypes")
    public static Object fromXML(String xml, Class[] types) {
        XStream stream = new XStream(new StaxDriver());
        stream.processAnnotations(types);
        return stream.fromXML(xml);
    }

    /**
     * 格式化日期，将日期转换为yyyy-MM-dd HH:mm:ss格式.
     *
     * @param date date to be format.
     * @return formatted string, like yyyy-MM-dd HH:mm:ss
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 解析日期字符串，将字符串转换为Date对象.
     *
     * @param dateStr which to parse.
     * @return a Date instance.
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将map中的所有key由驼峰式改为下划线式
     *
     * @param camelKeyMap
     * @return
     */
    public static Map<String, Object> camelKeyToUnderScore(Map<String, Object> camelKeyMap) {
        Map<String, Object> resultMap = new HashMap<>();
        camelKeyMap.forEach((key, value) -> resultMap.put(camelToUnderScore(key), value));
        return resultMap;
    }

    /**
     * 将一个驼峰式字符串改为一个下划线式字符串
     *
     * @param camel
     * @return
     */
    public static String camelToUnderScore(String camel) {
        if (StringUtils.isEmpty(camel)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = camel.toCharArray();
        for (char aChar : chars) {
            if (Character.isUpperCase(aChar)) {
                builder.append('_');
                builder.append(Character.toLowerCase(aChar));
            } else {
                builder.append(aChar);
            }
        }
        return builder.toString();
    }

    /**
     * 将一个pojo类转换为map
     *
     * @param object
     * @return map
     */
    public static Map<String, Object> obj2Map(Object object) {
        String objJson = JSON.toJSONString(object);
        return JSON.parseObject(objJson, Map.class);
    }

    /**
     * 把一个Object转换成指定pojo类
     *
     * @param object
     * @param clazz
     * @return
     */
    public static <T> T obj2Obj(Object object, Class<T> clazz) {
        String objJson = JSON.toJSONString(object);
        return JSON.parseObject(objJson, clazz);
    }

    /**
     * 将一个pojo类转换为key为下划线式的map
     *
     * @param object
     * @return
     */
    public static Map<String, Object> obj2UnderScoreMap(Object object) {
        return camelKeyToUnderScore(obj2Map(object));
    }

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

    /**
     * 把Map中的参数拼入url.
     *
     * @param url
     * @param params
     * @return
     */
    public static String putParam2Url(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String result = sb.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * 把Object转为String, 如果传入null，则返回null
     * @param str
     * @return
     */
    public static String getStrDealNull(Object str) {
        if (str == null) {
            return null;
        }
        return str.toString();
    }

    /**
     * 指定多个key在map中为空（包括空字符串）的数量
     * @param map
     * @param keys
     * @return count
     */
    public static boolean anyEmptyKeyInMap(Map<String, Object> map, String...keys) {
        return Stream.of(keys).anyMatch(key -> StringUtils.isEmpty(map.get(key)));
    }


}

/**
 * Date Converter.
 */
class DateConverter implements Converter {

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return Date.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Date date = (Date) source;
        writer.setValue(FormatUtil.formatDate(date));
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }
}