package com.haiercash.spring.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
public final class RabbitUtils {
    private RabbitUtils() {
    }

    private static RabbitTemplate getRabbitTemplate() {
        return RabbitTemplateProvider.getRabbitTemplate();
    }

    public static void convertAndSend(RabbitInfo rabbitInfo, Object object) {
        Assert.notNull(rabbitInfo, "rabbitInfo can not be null");
        Assert.notNull(object, "object can not be null");

        RabbitTemplate rabbitTemplate = getRabbitTemplate();
        rabbitTemplate.convertAndSend(rabbitInfo.getExchange(), rabbitInfo.getRoutingKey(), object);
    }
}
