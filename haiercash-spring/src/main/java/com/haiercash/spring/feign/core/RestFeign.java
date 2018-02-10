package com.haiercash.spring.feign.core;

import com.haiercash.core.reflect.ReflectionUtils;
import feign.Contract;
import feign.Feign;
import feign.InvocationHandlerFactory;

import java.lang.reflect.Field;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
public final class RestFeign {
    public static RestFeign.Builder builder() {
        return new RestFeign.Builder();
    }

    public static final class Builder extends Feign.Builder {
        private static final Field CONTRACT = ReflectionUtils.getFieldInfo(Feign.Builder.class, "contract", false);

        private Contract getContract() {
            if (CONTRACT == null)
                throw new IllegalStateException("get contract field of class Feign.Builder fail");
            try {
                return (Contract) CONTRACT.get(this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        public Feign build() {
            SynchronousMethodHandler.Factory methodHandlerFactory = new SynchronousMethodHandler.Factory();
            RestReflectiveFeign.ParseHandlersByName parseHandlersByName = new RestReflectiveFeign.ParseHandlersByName(this.getContract(), methodHandlerFactory);
            InvocationHandlerFactory invocationHandlerFactory = new FeignInvocationHandler.Factory();
            return new RestReflectiveFeign(parseHandlersByName, invocationHandlerFactory);
        }
    }
}
