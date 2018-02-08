package com.haiercash.core.reflect;

import com.bestvike.linq.exception.NotSupportedException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by 许崇雷 on 2018-02-08.
 */
public final class TypeUtils extends org.apache.commons.lang3.reflect.TypeUtils {
    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>)
            return (Class<?>) type;
        if (type instanceof ParameterizedType)
            return getRawClass(((ParameterizedType) type).getRawType());
        throw new NotSupportedException("Type must be instance of Class or ParameterizedType: " + type);
    }
}
