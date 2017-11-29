package com.haiercash.spring.rabbit.core;

import com.bestvike.linq.Linq;
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
public final class RabbitMultiMethodRabbitListenerEndpoint extends MultiMethodRabbitListenerEndpoint {
    private final MultiMethodRabbitListenerEndpoint endpoint;
    private final List<Method> methods;

    public RabbitMultiMethodRabbitListenerEndpoint(MultiMethodRabbitListenerEndpoint endpoint, List<Method> methods, Object bean) {
        super(methods, bean);
        this.endpoint = endpoint;
        this.methods = methods;
        this.init();
    }

    @Override
    protected HandlerAdapter configureListenerAdapter(MessagingMessageListenerAdapter messageListener) {
        List<InvocableHandlerMethod> invocableHandlerMethods = new ArrayList<>();
        for (Method method : this.methods)
            invocableHandlerMethods.add(this.getMessageHandlerMethodFactory().createInvocableHandlerMethod(this.getBean(), method));
        DelegatingInvocableHandler delegatingHandler = new RabbitDelegatingInvocableHandler(invocableHandlerMethods, this.getBean(), this.getResolver(), this.getBeanExpressionContext());
        return new HandlerAdapter(delegatingHandler);
    }

    private void init() {
        this.setBeanFactory(ReflectionUtils.invoke(this.endpoint, "getBeanFactory"));
        this.setMessageHandlerMethodFactory(ReflectionUtils.invoke(this.endpoint, "getMessageHandlerMethodFactory"));
        this.setId(this.endpoint.getId());
        this.setQueueNames(Linq.asEnumerable(this.endpoint.getQueueNames()).toArray(String.class));
        this.setGroup(this.endpoint.getGroup());
        this.setExclusive(this.endpoint.isExclusive());
        this.setPriority(this.endpoint.getPriority());
        this.setAdmin(this.endpoint.getAdmin());
    }
}
