package com.haiercash.spring.rabbit.core;

import com.haiercash.core.reflect.ReflectionUtils;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;

/**
 * Created by 许崇雷 on 2017-11-30.
 */
public final class RabbitMessageHandlerMethodFactory extends DefaultMessageHandlerMethodFactory {
    @Override
    public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
        InvocableHandlerMethod handlerMethod = new RabbitInvocableHandlerMethod(bean, method);
        handlerMethod.setMessageMethodArgumentResolvers(ReflectionUtils.getField(this, "argumentResolvers"));
        return handlerMethod;
    }
}
