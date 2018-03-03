package com.haiercash.spring.mail;

import com.bestvike.linq.Linq;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.spring.eureka.EurekaServer;
import com.haiercash.spring.mail.core.Mail;
import com.haiercash.spring.mail.core.MailAttachment;
import com.haiercash.spring.mail.core.MailInline;
import com.haiercash.spring.mail.core.MailType;
import com.haiercash.spring.mail.service.MailController;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
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
            helper.setText(mail.getContent(), mail.getMailType() == MailType.HTML);
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

    //转给消息平台
    private void sendForward(Mail mail) {
        String url = EurekaServer.APPMSG + MailController.API_SEND_MAIL;
        IResponse response = CommonRestUtils.postForMap(url, mail);
        if (response.isSuccess())
            return;
        this.logger.warn(String.format("消息平台发送邮件失败:%s-%s", response.getRetFlag(), response.getRetMsg()));
    }

    //发送
    public void send(Mail mail) {
        boolean forward = this.properties.getForward() != null && this.properties.getForward();//默认不转发
        if (forward)
            this.sendForward(mail);
        else
            this.sendDirect(mail);
    }
}
