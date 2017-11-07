package com.haiercash.payplatform.rabbit;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by 许崇雷 on 2017-11-03.
 */
public final class RabbitTemplateEx extends RabbitTemplate {
    public RabbitTemplateEx(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }
}
