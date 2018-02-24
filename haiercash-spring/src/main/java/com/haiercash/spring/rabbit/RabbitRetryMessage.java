package com.haiercash.spring.rabbit;

import lombok.Data;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2018-01-21.
 */
@Data
public final class RabbitRetryMessage {
    public static final String CONSUME_RETRY_NAME = "consumeRetry";
    public static final int CONSUME_RETRY_COUNT = 5;

    private final Message<?> message;
    private final int retry;

    public RabbitRetryMessage(Message<?> message, int retry) {
        Assert.notNull(message, "message can not be null");
        Assert.isInstanceOf(byte[].class, message.getPayload());

        this.message = message;
        this.retry = retry;
    }

    public Object getHeader(String key) {
        return this.message.getHeaders().get(key);
    }

    public MessageHeaders getHeaders() {
        return this.message.getHeaders();
    }

    public byte[] getPayload() {
        return ((byte[]) this.message.getPayload());
    }
}
