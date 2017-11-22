package com.haiercash.spring.mail;

import com.haiercash.spring.mail.core.Mail;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
public final class MailUtils {
    private MailUtils() {
    }

    private static MailTemplate getMailTemplate() {
        return MailTemplateProvider.getMailTemplate();
    }

    public static void send(Mail mail) {
        getMailTemplate().send(mail);
    }
}
