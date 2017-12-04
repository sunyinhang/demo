package com.haiercash.spring.rabbit.core;

import org.springframework.amqp.rabbit.listener.adapter.DelegatingInvocableHandler;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-28.
 */
public final class RabbitDelegatingInvocableHandler extends DelegatingInvocableHandler {
    private final List<InvocableHandlerMethod> handlers;

    public RabbitDelegatingInvocableHandler(List<InvocableHandlerMethod> handlers, Object bean, BeanExpressionResolver beanExpressionResolver, BeanExpressionContext beanExpressionContext) {
        super(handlers, bean, beanExpressionResolver, beanExpressionContext);
        this.handlers = new ArrayList<>(handlers);
    }

    @Override
    protected InvocableHandlerMethod findHandlerForPayload(Class<?> payloadClass) {
        return this.handlers.size() == 1 ? this.handlers.get(0) : super.findHandlerForPayload(payloadClass);
    }
}
