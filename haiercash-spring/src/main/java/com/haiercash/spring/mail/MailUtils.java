package com.haiercash.spring.mail;

import com.haiercash.spring.mail.core.Mail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
public final class MailUtils {
    private static final Log logger = LogFactory.getLog(Log.class);

    private MailUtils() {
    }

    private static MailTemplate getMailTemplate() {
        return MailTemplateProvider.getMailTemplate();
    }

    public static void send(Mail mail) {
        MailTemplate mailTemplate = getMailTemplate();
        if (mailTemplate == null) {
            logger.warn("由于未启用 smtp 功能，跳过了发送邮件");
            return;
        }
        mailTemplate.send(mail);
    }
}
