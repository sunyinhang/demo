package com.haiercash.spring.feign.core;

import feign.InvocationHandlerFactory;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
public final class SynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {
    private FeignRequest.Factory requestFactory;

    private SynchronousMethodHandler(FeignRequest.Factory requestFactory) {
        Assert.notNull(requestFactory, "requestFactory can not be null");
        this.requestFactory = requestFactory;
    }

    @Override
    public Object invoke(Object[] argv) {
        return this.requestFactory.create(argv).invoke();
    }


    static class Factory {
        public InvocationHandlerFactory.MethodHandler create(FeignRequest.Factory requestFactory) {
            return new SynchronousMethodHandler(requestFactory);
        }
    }
}
