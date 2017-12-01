package com.haiercash.spring.rabbit.core;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Created by 许崇雷 on 2017-12-01.
 */
public final class RabbitSimpleListenerContainerFactory extends SimpleRabbitListenerContainerFactory {
    public RabbitSimpleListenerContainerFactory() {
        super();
        this.setErrorHandler(new RabbitConditionalRejectingErrorHandler());
    }

    @Override
    protected SimpleMessageListenerContainer createContainerInstance() {
        return new RabbitSimpleListenerContainer();
    }
}
