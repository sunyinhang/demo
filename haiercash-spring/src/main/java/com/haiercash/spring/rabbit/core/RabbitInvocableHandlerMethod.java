package com.haiercash.spring.rabbit.core;

import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;

/**
 * Created by 许崇雷 on 2017-11-30.
 */
public final class RabbitInvocableHandlerMethod extends InvocableHandlerMethod {
    public RabbitInvocableHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }
}
