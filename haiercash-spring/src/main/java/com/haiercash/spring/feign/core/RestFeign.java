package com.haiercash.spring.feign.core;

import com.haiercash.core.reflect.ReflectionUtils;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.InvocationHandlerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;

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
        private static final Field CLIENT = ReflectionUtils.getFieldInfo(Feign.Builder.class, "client", false);

        private Contract getContract() {
            if (CONTRACT == null)
                throw new IllegalStateException("get contract field of class Feign.Builder fail");
            try {
                return (Contract) CONTRACT.get(this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        private Client getClient() {
            if (CLIENT == null)
                throw new IllegalStateException("get client field of class Feign.Builder fail");
            try {
                return (Client) CLIENT.get(this);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        public Feign build() {
            boolean loadBalanced = this.getClient() instanceof LoadBalancerFeignClient;
            SynchronousMethodHandler.Factory methodHandlerFactory = new SynchronousMethodHandler.Factory(loadBalanced);
            RestReflectiveFeign.ParseHandlersByName parseHandlersByName = new RestReflectiveFeign.ParseHandlersByName(this.getContract(), methodHandlerFactory);
            InvocationHandlerFactory invocationHandlerFactory = new FeignInvocationHandler.Factory();
            return new RestReflectiveFeign(parseHandlersByName, invocationHandlerFactory);
        }
    }
}
