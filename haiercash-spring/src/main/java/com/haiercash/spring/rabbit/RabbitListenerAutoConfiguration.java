package com.haiercash.spring.rabbit;

import com.haiercash.core.reflect.ReflectionUtils;
import com.haiercash.spring.rabbit.converter.MappingFastJsonRabbitMessageConverter;
import com.haiercash.spring.rabbit.core.RabbitMultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-27.
 */
@Configuration
public class RabbitListenerAutoConfiguration implements RabbitListenerConfigurer {
    private void setHandlerMethodFactory(RabbitListenerEndpointRegistrar registrar) {
        DefaultMessageHandlerMethodFactory handlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        handlerMethodFactory.setMessageConverter(new MappingFastJsonRabbitMessageConverter());
        handlerMethodFactory.setBeanFactory(ReflectionUtils.getField(registrar, "beanFactory"));
        handlerMethodFactory.afterPropertiesSet();
        registrar.setMessageHandlerMethodFactory(handlerMethodFactory);
    }

    private void wrapListenerEndpoints(RabbitListenerEndpointRegistrar registrar) {
        List<?> endpointDescriptors = ReflectionUtils.getField(registrar, "endpointDescriptors");
        List<?> endpointDescriptorsCopy = new ArrayList<>(endpointDescriptors);
        endpointDescriptors.clear();
        for (Object endpointDescriptor : endpointDescriptorsCopy) {
            RabbitListenerEndpoint endpoint = ReflectionUtils.getField(endpointDescriptor, "endpoint");
            RabbitListenerContainerFactory<?> containerFactory = ReflectionUtils.getField(endpointDescriptor, "containerFactory");
            //替换 endpoint
            if (endpoint instanceof MultiMethodRabbitListenerEndpoint) {
                MultiMethodRabbitListenerEndpoint multiEndpoint = ((MultiMethodRabbitListenerEndpoint) endpoint);
                endpoint = new RabbitMultiMethodRabbitListenerEndpoint(multiEndpoint, ReflectionUtils.getField(multiEndpoint, "methods"), multiEndpoint.getBean());
            }
            registrar.registerEndpoint(endpoint, containerFactory);
        }
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        this.setHandlerMethodFactory(registrar);
        this.wrapListenerEndpoints(registrar);
    }
}
