package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.rabbit.RabbitRetryMessage;
import com.haiercash.spring.trace.TraceConfig;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-27.
 */
public final class RabbitLogUtils {
    public static String getMsgId(Message message) {
        MessageHeaders headers = message.getHeaders();
        String msgId = Convert.toString(headers.get(AmqpHeaders.MESSAGE_ID));
        return StringUtils.isEmpty(msgId) ? headers.getId().toString() : msgId;
    }

    public static String getMsgId(org.springframework.amqp.core.Message message) {
        return message.getMessageProperties().getMessageId();
    }

    public static String getRetry(Message message) {
        int retry = Convert.defaultInteger(message.getHeaders().get(RabbitRetryMessage.CONSUME_RETRY_NAME));
        return retry == 0 ? "初次消费" : String.format("第 %d 次重试消费", retry);
    }

    public static Map<String, Object> getHeaders(Message message) {
        return message.getHeaders();
    }

    public static Map<String, Object> getHeaders(org.springframework.amqp.core.Message message) {
        MessageProperties properties = message.getMessageProperties();
        Map<String, Object> headers = new LinkedHashMap<>(2);
        headers.put("content-type", properties.getContentType());
        headers.put("content-encoding", properties.getContentEncoding());
        if (MapUtils.isNotEmpty(properties.getHeaders()))
            headers.put("headers", properties.getHeaders());
        return headers;
    }

    public static String getBody(Message message) {
        return getBodyCore((byte[]) message.getPayload(), Convert.toString(message.getHeaders().get(AmqpHeaders.CONTENT_ENCODING)));
    }

    public static String getBody(org.springframework.amqp.core.Message message) {
        return getBodyCore(message.getBody(), message.getMessageProperties().getContentEncoding());
    }

    private static String getBodyCore(byte[] body, String encoding) {
        encoding = Convert.defaultString(encoding, TraceConfig.DEFAULT_CHARSET_NAME);
        try {
            return new String(body, encoding);
        } catch (Exception e) {
            return TraceConfig.BODY_PARSE_FAIL;
        }
    }
}
