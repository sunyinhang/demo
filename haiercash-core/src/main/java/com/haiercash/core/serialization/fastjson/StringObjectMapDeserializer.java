package com.haiercash.core.serialization.fastjson;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.MapDeserializer;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-26.
 */
public final class StringObjectMapDeserializer extends MapDeserializer {
    public static StringObjectMapDeserializer instance = new StringObjectMapDeserializer();

    private StringObjectMapDeserializer() {
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object deserialze(DefaultJSONParser parser, Type type, Object fieldName, Map map) {
        return parseMap(parser, map, Object.class, fieldName);
    }
}
