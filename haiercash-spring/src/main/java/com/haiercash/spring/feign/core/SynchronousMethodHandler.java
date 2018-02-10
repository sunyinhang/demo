package com.haiercash.spring.feign.core;

import feign.InvocationHandlerFactory;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
public final class SynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {
    private final FeignRequest.Factory requestFactory;
    private final boolean loadBalanced;

    private SynchronousMethodHandler(FeignRequest.Factory requestFactory, boolean loadBalanced) {
        Assert.notNull(requestFactory, "requestFactory can not be null");
        this.requestFactory = requestFactory;
        this.loadBalanced = loadBalanced;
    }

    @Override
    public Object invoke(Object[] argv) {
        return this.requestFactory.create(argv).invoke(this.loadBalanced);
    }


    static class Factory {
        private final boolean loadBalanced;

        Factory(boolean loadBalanced) {
            this.loadBalanced = loadBalanced;
        }

        public InvocationHandlerFactory.MethodHandler create(FeignRequest.Factory requestFactory) {
            return new SynchronousMethodHandler(requestFactory, this.loadBalanced);
        }
    }
}
