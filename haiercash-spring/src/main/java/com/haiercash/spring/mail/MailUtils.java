package com.haiercash.spring.mail;

import com.bestvike.linq.Linq;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.lang.Convert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
public final class MailUtils {
    private static final String PROPERTY_SMTP_MAIL_FROM = "smtp.mail.from";
    private static final Log logger = LogFactory.getLog(MailUtils.class);

    private static JavaMailSenderImpl getJavaMailSender() {
        return (JavaMailSenderImpl) MailSenderProvider.getJavaMailSender();
    }

    public static void send(Mail mail) {
        JavaMailSenderImpl mailSender = getJavaMailSender();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, CollectionUtils.isNotEmpty(mail.getInlineList()) || CollectionUtils.isNotEmpty(mail.getAttachmentList()));
            helper.setFrom(Convert.toString(mailSender.getJavaMailProperties().get(PROPERTY_SMTP_MAIL_FROM)), mail.getDisplay());
            helper.setTo(Linq.asEnumerable(mail.getToList()).toArray(String.class));
            helper.setCc(Linq.asEnumerable(mail.getCcList()).toArray(String.class));
            helper.setBcc(Linq.asEnumerable(mail.getBccList()).toArray(String.class));
            helper.setSubject(mail.getSubject());
            helper.setText(mail.getContent(), mail.getMailType() == MailType.Html);
            if (CollectionUtils.isNotEmpty(mail.getInlineList()))
                for (MailInline inline : mail.getInlineList())
                    helper.addInline(inline.getContentId(), inline.getFile());
            if (CollectionUtils.isNotEmpty(mail.getAttachmentList()))
                for (MailAttachment attachment : mail.getAttachmentList())
                    helper.addAttachment(attachment.getFileName(), attachment.getFile());
        } catch (Exception e) {
            logger.error("发送邮件失败", e);
        }
    }
}
