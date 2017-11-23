package com.haiercash.spring.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Component
public final class MailTemplateProvider {
    private static MailTemplate mailTemplate;
    @Autowired(required = false)
    private MailTemplate mailTemplateInstance;

    public static MailTemplate getMailTemplate() {
        return mailTemplate;
    }

    @PostConstruct
    private void init() {
        mailTemplate = mailTemplateInstance;
    }
}
