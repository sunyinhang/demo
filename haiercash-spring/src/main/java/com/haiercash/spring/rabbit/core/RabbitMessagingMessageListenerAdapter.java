package com.haiercash.spring.rabbit.core;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;

/**
 * Created by 许崇雷 on 2017-11-30.
 */
public final class RabbitMessagingMessageListenerAdapter extends MessagingMessageListenerAdapter {
    @Override
    public void onMessage(Message amqpMessage, Channel channel) throws Exception {
        super.onMessage(amqpMessage, channel);
    }
}
