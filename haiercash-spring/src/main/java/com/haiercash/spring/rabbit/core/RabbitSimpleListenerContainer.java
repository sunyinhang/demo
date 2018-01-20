package com.haiercash.spring.rabbit.core;

import com.haiercash.core.threading.ThreadPool;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Created by 许崇雷 on 2017-12-01.
 */
public final class RabbitSimpleListenerContainer extends SimpleMessageListenerContainer {
    public RabbitSimpleListenerContainer() {
        this.setTaskExecutor(ThreadPool.getExecutor());
    }

    public RabbitSimpleListenerContainer(ConnectionFactory connectionFactory) {
        super(connectionFactory);
        this.setTaskExecutor(ThreadPool.getExecutor());
    }
}
