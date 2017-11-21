package com.haiercash.spring.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Component
public final class MailSenderProvider {
    private static JavaMailSender javaMailSender;
    @Autowired
    private JavaMailSender javaMailSenderInstance;

    public static JavaMailSender getJavaMailSender() {
        return javaMailSender;
    }

    @PostConstruct
    private void init() {
        javaMailSender = javaMailSenderInstance;
    }
}
