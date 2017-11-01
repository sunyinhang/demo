package com.haiercash.payplatform.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Component
public final class RabbitTemplateProvider {
    private static RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplateInstance;

    public static RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    @PostConstruct
    private void init() {
        rabbitTemplate = this.rabbitTemplateInstance;
    }
}
