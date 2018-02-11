package com.haiercash.spring.feign.core;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.feign.annotation.FeignApi;
import com.haiercash.spring.trace.feign.OutgoingLog;
import feign.InvocationHandlerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
public final class DelegateMethodHandler implements InvocationHandlerFactory.MethodHandler {
    private final String name;
    private final Class<?> type;
    private final Method method;
    private final InvocationHandlerFactory.MethodHandler handler;

    public DelegateMethodHandler(String name, Class<?> type, Method method, InvocationHandlerFactory.MethodHandler handler) {
        Assert.hasLength(name, "name can not be empty");
        Assert.notNull(type, "type can not be null");
        Assert.notNull(method, "method can not be null");
        Assert.notNull(handler, "handler can not be null");
        FeignApi feignApi = method.getAnnotation(FeignApi.class);
        this.name = (feignApi == null || StringUtils.isEmpty(feignApi.value())) ? name : feignApi.value();
        this.type = type;
        this.method = method;
        this.handler = handler;
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getType() {
        return this.type;
    }

    public Method getMethod() {
        return this.method;
    }

    public InvocationHandlerFactory.MethodHandler getHandler() {
        return this.handler;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        Map<String, Object> log = OutgoingLog.writeBeginLog(this);
        long begin = System.currentTimeMillis();
        try {
            return this.handler.invoke(argv);
        } finally {
            OutgoingLog.writeEndLog(log, System.currentTimeMillis() - begin);
        }
    }
}
