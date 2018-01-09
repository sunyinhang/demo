package com.haiercash.core.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.haiercash.core.lang.DateUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
public final class XmlSerializer {
    private static final XmlMapper XML_MAPPER;

    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        SerializationConfig serializationConfig = XML_MAPPER.getSerializationConfig().with(DateUtils.dateTimeFormat());
        XML_MAPPER.setConfig(serializationConfig);
        DeserializationConfig deserializationConfig = XML_MAPPER.getDeserializationConfig().with(DateUtils.dateTimeFormat());
        XML_MAPPER.setConfig(deserializationConfig);
    }

    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化后的xml
     */
    public static String serialize(Object obj) {
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 序列化
     *
     * @param obj      要序列化的对象
     * @param rootName 根节点名字
     * @return 序列化后的xml
     */
    public static String serialize(Object obj, String rootName) {
        try {
            return XML_MAPPER.writer().withRootName(rootName).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化
     *
     * @param xml   字符串
     * @param clazz 类型
     * @return 对象
     */
    public static <T> T deserialize(String xml, Class<T> clazz) {
        try {
            return XML_MAPPER.readValue(xml, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化
     *
     * @param xml  字符串
     * @param type 类型引用
     * @param <T>
     * @return
     */
    public static <T> T deserialize(String xml, TypeReference<T> type) {
        try {
            return XML_MAPPER.readValue(xml, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化数组
     *
     * @param xml   字符串
     * @param clazz 类型
     * @return 对象列表
     */
    public static <T> List<T> deserializeArray(String xml, Class<T> clazz) {
        return deserialize(xml, new TypeReference<List<T>>() {
            @Override
            public Type getType() {
                return ParameterizedTypeImpl.make(List.class, new Type[]{clazz}, null);
            }
        });
    }

    /**
     * 反序列化
     *
     * @param xml 字符串
     * @return Map 实例
     */
    public static Map<String, Object> deserializeMap(String xml) {
        return deserialize(xml, new TypeReference<Map<String, Object>>() {
        });
    }
}
