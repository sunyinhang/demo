package com.haiercash.payplatform.rabbit;

import com.bestvike.serialization.JsonSerializer;
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

    public static void convertAndSend(RabbitProperty rabbitProperty, Object message) {
        Assert.notNull(rabbitProperty, "rabbitProperty can not be null");
        Assert.notNull(message, "message can not be null");

        RabbitTemplate rabbitTemplate = getRabbitTemplate();
        if (message instanceof String)
            rabbitTemplate.convertAndSend(rabbitProperty.getExchange(), rabbitProperty.getRoutingKey(), message);
        else
            rabbitTemplate.convertAndSend(rabbitProperty.getExchange(), rabbitProperty.getRoutingKey(), JsonSerializer.serialize(message));
    }
}
