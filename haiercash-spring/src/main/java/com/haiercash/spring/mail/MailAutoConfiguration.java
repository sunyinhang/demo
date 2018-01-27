package com.haiercash.spring.mail;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Configuration
@Conditional(MailAutoConfiguration.MailSenderCondition.class)
@EnableConfigurationProperties(MailProperties.class)
public class MailAutoConfiguration {
    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public MailAutoConfiguration(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    MailTemplate mailTemplate() {
        return new MailTemplate(this.mailSender, this.properties);
    }


    static class MailSenderCondition extends AnyNestedCondition {
        MailSenderCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(prefix = "spring.mail", name = "jndi-name")
        static class JndiNameProperty {
            JndiNameProperty() {
            }
        }

        @ConditionalOnProperty(prefix = "spring.mail", name = "host")
        static class HostProperty {
            HostProperty() {
            }
        }
    }
}
