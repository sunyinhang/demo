package com.haiercash.spring.rabbit.core;

import com.haiercash.core.reflect.ReflectionUtils;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.adapter.DelegatingInvocableHandler;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-28.
 */
public final class RabbitMultiMethodRabbitListenerEndpoint extends RabbitMethodRabbitListenerEndpoint {
    private List<Method> methods;

    public RabbitMultiMethodRabbitListenerEndpoint(MultiMethodRabbitListenerEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    protected void copyProperties() {
        this.methods = ReflectionUtils.getField(this.endpoint, "methods");
        super.copyProperties();
    }

    @Override
    protected MessagingMessageListenerAdapter createMessageListenerInstance() {
        return new RabbitMessagingMessageListenerAdapter();
    }

    @Override
    protected HandlerAdapter configureListenerAdapter(MessagingMessageListenerAdapter messageListener) {
        List<InvocableHandlerMethod> invocableHandlerMethods = new ArrayList<>();
        for (Method method : this.methods)
            invocableHandlerMethods.add(this.getMessageHandlerMethodFactory().createInvocableHandlerMethod(this.getBean(), method));
        DelegatingInvocableHandler delegatingHandler = new RabbitDelegatingInvocableHandler(invocableHandlerMethods, this.getBean(), this.getResolver(), this.getBeanExpressionContext());
        return new RabbitHandlerAdapter(delegatingHandler);
    }
}
