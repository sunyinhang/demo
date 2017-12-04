package com.haiercash.spring.rabbit;

import com.haiercash.spring.trace.rabbit.OutgoingLog;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * Created by 许崇雷 on 2017-11-03.
 */
public final class RabbitTemplateEx extends RabbitTemplate {
    public RabbitTemplateEx(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    protected void doSend(Channel channel, String exchange, String routingKey, Message message, boolean mandatory, CorrelationData correlationData) throws Exception {
        StringBuilder builder = OutgoingLog.writeRequestLog(message, exchange, routingKey);
        long begin = System.currentTimeMillis();
        try {
            super.doSend(channel, exchange, routingKey, message, mandatory, correlationData);
            OutgoingLog.writeResponseLog(builder, System.currentTimeMillis() - begin);
        } catch (Exception e) {
            OutgoingLog.writeErrorLog(builder, e, System.currentTimeMillis() - begin);
            throw e;
        }
    }
}
