package com.haiercash.spring.mail;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailAutoConfiguration {
    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public MailAutoConfiguration(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Bean
    MailTemplate mailTemplate() {
        return new MailTemplate(this.mailSender, this.properties);
    }
}
