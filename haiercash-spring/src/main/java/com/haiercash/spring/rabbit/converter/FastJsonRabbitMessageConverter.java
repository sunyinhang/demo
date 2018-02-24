package com.haiercash.spring.rabbit.converter;

import com.bestvike.linq.exception.ArgumentNullException;
import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.rabbit.RabbitRetryMessage;
import com.haiercash.spring.trace.rabbit.RabbitLogUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.WhiteListDeserializingMessageConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.util.ClassUtils;

import java.io.UnsupportedEncodingException;

/**
 * Created by 许崇雷 on 2017-11-27.
 */
public final class FastJsonRabbitMessageConverter extends WhiteListDeserializingMessageConverter implements BeanClassLoaderAware {
    private static final String DEFAULT_CHARSET = CharsetNames.UTF_8;
    private volatile String defaultCharset = DEFAULT_CHARSET;
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    public String getDefaultCharset() {
        return this.defaultCharset;
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = Convert.defaultString(defaultCharset, DEFAULT_CHARSET);
    }

    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        return message.getBody();
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        if (object == null)
            throw new ArgumentNullException("object", "object can not be null.");
        byte[] bytes;
        if (object instanceof byte[]) {
            bytes = (byte[]) object;
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
        } else if (object instanceof String) {
            try {
                bytes = ((String) object).getBytes(this.defaultCharset);
            } catch (UnsupportedEncodingException e) {
                throw new MessageConversionException("failed to convert to Message content", e);
            }
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
            messageProperties.setContentEncoding(this.defaultCharset);
        } else if (object instanceof RabbitRetryMessage) {
            RabbitRetryMessage retryMessage = (RabbitRetryMessage) object;
            bytes = retryMessage.getPayload();
            messageProperties.setMessageId(RabbitLogUtils.getMsgId(retryMessage.getMessage()));
            messageProperties.setContentType(Convert.toString(retryMessage.getHeader(AmqpHeaders.CONTENT_TYPE)));
            messageProperties.setContentEncoding(Convert.toString(retryMessage.getHeader(AmqpHeaders.CONTENT_ENCODING)));
            messageProperties.setHeader(RabbitRetryMessage.CONSUME_RETRY_NAME, retryMessage.getRetry());
        } else {
            try {
                String json = JsonSerializer.serialize(object);
                bytes = json.getBytes(this.defaultCharset);
            } catch (UnsupportedEncodingException e) {
                throw new MessageConversionException("failed to convert to json Message content", e);
            }
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setContentEncoding(this.defaultCharset);
        }
        messageProperties.setContentLength(bytes.length);
        return new Message(bytes, messageProperties);
    }
}
