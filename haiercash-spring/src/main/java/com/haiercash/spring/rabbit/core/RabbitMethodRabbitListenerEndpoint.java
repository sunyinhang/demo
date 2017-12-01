package com.haiercash.spring.rabbit.core;

import com.bestvike.linq.Linq;
import com.haiercash.core.reflect.ReflectionUtils;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

/**
 * Created by 许崇雷 on 2017-11-30.
 */
public class RabbitMethodRabbitListenerEndpoint extends MethodRabbitListenerEndpoint {
    protected final MethodRabbitListenerEndpoint endpoint;

    public RabbitMethodRabbitListenerEndpoint(MethodRabbitListenerEndpoint endpoint) {
        this.endpoint = endpoint;
        this.copyProperties();
    }

    protected void copyProperties() {
        this.setMethod(this.endpoint.getMethod());
        this.setBean(this.endpoint.getBean());
        this.setBeanFactory(ReflectionUtils.invoke(this.endpoint, "getBeanFactory"));
        this.setMessageHandlerMethodFactory(ReflectionUtils.invoke(this.endpoint, "getMessageHandlerMethodFactory"));
        this.setId(this.endpoint.getId());
        this.setQueueNames(Linq.asEnumerable(this.endpoint.getQueueNames()).toArray(String.class));
        this.setGroup(this.endpoint.getGroup());
        this.setExclusive(this.endpoint.isExclusive());
        this.setPriority(this.endpoint.getPriority());
        this.setAdmin(this.endpoint.getAdmin());
    }

    @Override
    protected MessagingMessageListenerAdapter createMessageListenerInstance() {
        return new RabbitMessagingMessageListenerAdapter();
    }

    @Override
    protected HandlerAdapter configureListenerAdapter(MessagingMessageListenerAdapter messageListener) {
        InvocableHandlerMethod invocableHandlerMethod = this.getMessageHandlerMethodFactory().createInvocableHandlerMethod(this.getBean(), this.getMethod());
        return new RabbitHandlerAdapter(invocableHandlerMethod);
    }
}
