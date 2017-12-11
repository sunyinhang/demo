package com.haiercash.spring.mail.service;

import com.haiercash.spring.mail.MailUtils;
import com.haiercash.spring.mail.core.Mail;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Created by 许崇雷 on 2017-12-11.
 */
@Component
@ConditionalOnProperty(name = "spring.mail.async", havingValue = "true")
public final class MainConsumer {
    @RabbitListener(queues = "${spring.mail.rabbit.queue}")
    public void consumeMail(@Payload Mail mail) {
        MailUtils.send(mail);
    }
}
