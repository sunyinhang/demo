package com.haiercash.spring.mail;

import com.bestvike.linq.Linq;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.spring.mail.core.Mail;
import com.haiercash.spring.mail.core.MailAttachment;
import com.haiercash.spring.mail.core.MailInline;
import com.haiercash.spring.mail.core.MailSendMode;
import com.haiercash.spring.mail.core.MailType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
public final class MailTemplate {
    private final JavaMailSender mailSender;
    private final MailProperties properties;
    private final Log logger = LogFactory.getLog(MailUtils.class);

    public MailTemplate(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    //直接发送
    private void sendDirect(Mail mail) {
        try {
            MimeMessage message = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, CollectionUtils.isNotEmpty(mail.getInlineList()) || CollectionUtils.isNotEmpty(mail.getAttachmentList()));
            helper.setFrom(this.properties.getUsername(), mail.getDisplay());
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
            this.mailSender.send(message);
        } catch (Exception e) {
            this.logger.error("发送邮件失败", e);
        }
    }

    //通过消息平台发送
    private void sendMsgPlatform(Mail mail) {
    }

    //发送
    public void send(Mail mail) {
        String mode = Convert.toString(this.properties.getMode()).toLowerCase();
        switch (mode) {
            case MailSendMode.DIRECT:
                this.sendDirect(mail);
                break;
            case MailSendMode.MSGPLATFORM:
                this.sendMsgPlatform(mail);
                break;
            default:
                this.sendDirect(mail);
                break;
        }
    }
}
