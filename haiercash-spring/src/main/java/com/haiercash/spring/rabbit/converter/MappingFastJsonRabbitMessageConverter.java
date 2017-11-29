package com.haiercash.spring.rabbit.converter;

import com.bestvike.linq.exception.NotSupportedException;
import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.serialization.JsonSerializer;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;

import java.util.Collections;

/**
 * Created by 许崇雷 on 2017-11-27.
 */
public final class MappingFastJsonRabbitMessageConverter extends AbstractMessageConverter {
    private static final String DEFAULT_CHARSET = CharsetNames.UTF_8;
    private volatile String defaultCharset = DEFAULT_CHARSET;

    public MappingFastJsonRabbitMessageConverter() {
        super(Collections.emptyList());
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    private String getBodyAsString(byte[] payload, MessageHeaders headers) {
        String encoding = headers == null ? this.defaultCharset : Convert.defaultString(headers.get(AmqpHeaders.CONTENT_ENCODING), this.defaultCharset);
        try {
            return new String(payload, encoding);
        } catch (Exception e) {
            throw new MessageConversionException("failed convert payload to String using " + encoding, e);
        }
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        if (!(message.getPayload() instanceof byte[]))
            throw new RuntimeException("payload must instance of byte[]");
        byte[] payload = (byte[]) message.getPayload();
        if (byte[].class.isAssignableFrom(targetClass))
            return payload;
        String json = this.getBodyAsString(payload, message.getHeaders());
        if (String.class.isAssignableFrom(targetClass))
            return json;
        return JsonSerializer.deserialize(json, targetClass);
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        throw new NotSupportedException(String.format("not support convertToInternal payload:%s headers:%s conversionHint:%s ", payload, headers, conversionHint));
    }
}
