package com.haiercash.payplatform.rest;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.Assert;

import java.lang.reflect.Type;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public class ParameterizedTypeRef<T> extends ParameterizedTypeReference<T> {
    private final Type type;

    public ParameterizedTypeRef(Type type) {
        super();
        Assert.notNull(type, "type can not be null.");
        this.type = type;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof org.springframework.core.ParameterizedTypeReference && this.type.equals(((ParameterizedTypeRef) obj).type);
    }

    @Override
    public int hashCode() {
        return this.type.hashCode();
    }

    @Override
    public String toString() {
        return "ParameterizedTypeRef<" + this.type + ">";
    }
}
