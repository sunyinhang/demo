package com.haiercash.spring.rabbit;

import com.haiercash.core.reflect.ReflectionUtils;
import com.haiercash.spring.rabbit.converter.MappingFastJsonRabbitMessageConverter;
import com.haiercash.spring.rabbit.core.RabbitMessageHandlerMethodFactory;
import com.haiercash.spring.rabbit.core.RabbitMethodRabbitListenerEndpoint;
import com.haiercash.spring.rabbit.core.RabbitMultiMethodRabbitListenerEndpoint;
import com.haiercash.spring.rabbit.core.RabbitSimpleListenerContainerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-27.
 */
@Configuration
public class RabbitListenerAutoConfiguration implements RabbitListenerConfigurer {
    @Bean
    @ConditionalOnMissingBean(name = "rabbitListenerContainerFactory")
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new RabbitSimpleListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        this.setHandlerMethodFactory(registrar);
        this.wrapListenerEndpoints(registrar);
    }

    private void setHandlerMethodFactory(RabbitListenerEndpointRegistrar registrar) {
        RabbitMessageHandlerMethodFactory handlerMethodFactory = new RabbitMessageHandlerMethodFactory();
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
            if (endpoint instanceof MultiMethodRabbitListenerEndpoint)
                endpoint = new RabbitMultiMethodRabbitListenerEndpoint((MultiMethodRabbitListenerEndpoint) endpoint);
            else if (endpoint instanceof MethodRabbitListenerEndpoint)
                endpoint = new RabbitMethodRabbitListenerEndpoint((MethodRabbitListenerEndpoint) endpoint);
            else
                throw new RuntimeException("unexpected type of RabbitListenerEndpoint");
            registrar.registerEndpoint(endpoint, containerFactory);
        }
    }
}
