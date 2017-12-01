package com.haiercash.spring.rabbit;

import com.haiercash.spring.rabbit.converter.FastJsonRabbitMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Created by 许崇雷 on 2017-11-03.
 */
@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitAutoConfiguration {
    private final RabbitProperties properties;

    public RabbitAutoConfiguration(RabbitProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    MessageConverter messageConverter() {
        return new FastJsonRabbitMessageConverter();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplateEx(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(this.determineMandatoryFlag());
        RabbitProperties.Template templateProperties = this.properties.getTemplate();
        RabbitProperties.Retry retryProperties = templateProperties.getRetry();
        if (retryProperties.isEnabled())
            rabbitTemplate.setRetryTemplate(this.createRetryTemplate(retryProperties));
        if (templateProperties.getReceiveTimeout() != null)
            rabbitTemplate.setReceiveTimeout(templateProperties.getReceiveTimeout());
        if (templateProperties.getReplyTimeout() != null)
            rabbitTemplate.setReplyTimeout(templateProperties.getReplyTimeout());
        return rabbitTemplate;
    }

    private boolean determineMandatoryFlag() {
        Boolean mandatory = this.properties.getTemplate().getMandatory();
        return mandatory != null ? mandatory : this.properties.isPublisherReturns();
    }

    private RetryTemplate createRetryTemplate(RabbitProperties.Retry properties) {
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(properties.getMaxAttempts());
        template.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getInitialInterval());
        backOffPolicy.setMultiplier(properties.getMultiplier());
        backOffPolicy.setMaxInterval(properties.getMaxInterval());
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }
}
