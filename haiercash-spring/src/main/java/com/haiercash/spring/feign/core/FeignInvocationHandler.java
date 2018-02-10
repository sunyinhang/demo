package com.haiercash.spring.feign.core;

import feign.InvocationHandlerFactory;
import feign.Target;
import feign.Util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
public final class FeignInvocationHandler implements InvocationHandler {
    private final Target target;
    private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;

    FeignInvocationHandler(Target target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
        this.target = Util.checkNotNull(target, "target");
        this.dispatch = Util.checkNotNull(dispatch, "dispatch for %s", target);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "equals":
                try {
                    Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                    return this.equals(otherHandler);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            case "hashCode":
                return this.hashCode();
            case "toString":
                return this.toString();
            default:
                return this.dispatch.get(method).invoke(args);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeignInvocationHandler) {
            FeignInvocationHandler other = (FeignInvocationHandler) obj;
            return this.target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    @Override
    public String toString() {
        return this.target.toString();
    }


    static class Factory implements InvocationHandlerFactory {
        @Override
        public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
            return new FeignInvocationHandler(target, dispatch);
        }
    }
}
