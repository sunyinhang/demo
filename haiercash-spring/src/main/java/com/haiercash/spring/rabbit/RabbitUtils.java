package com.haiercash.spring.rabbit;

import com.haiercash.core.serialization.JsonSerializer;
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

    public static void convertAndSend(RabbitInfo rabbitInfo, Object message) {
        Assert.notNull(rabbitInfo, "rabbitInfo can not be null");
        Assert.notNull(message, "message can not be null");

        RabbitTemplate rabbitTemplate = getRabbitTemplate();
        if (message instanceof String)
            rabbitTemplate.convertAndSend(rabbitInfo.getExchange(), rabbitInfo.getRoutingKey(), message);
        else
            rabbitTemplate.convertAndSend(rabbitInfo.getExchange(), rabbitInfo.getRoutingKey(), JsonSerializer.serialize(message));
    }
}
