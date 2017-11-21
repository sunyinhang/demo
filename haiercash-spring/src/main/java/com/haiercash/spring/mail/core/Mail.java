package com.haiercash.spring.mail.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Data
public final class Mail {
    private String display;
    private List<String> toList;
    private List<String> ccList;
    private List<String> bccList;
    private String subject;
    private String content;
    private List<MailInline> inlineList;
    private List<MailAttachment> attachmentList;
    private MailType mailType = MailType.Plain;

    public void addTo(String to) {
        if (this.toList == null)
            this.toList = new ArrayList<>();
        this.toList.add(to);
    }

    public void addCc(String cc) {
        if (this.ccList == null)
            this.ccList = new ArrayList<>();
        this.ccList.add(cc);
    }

    public void addBcc(String bcc) {
        if (this.bccList == null)
            this.bccList = new ArrayList<>();
        this.bccList.add(bcc);
    }

    public void addInline(MailInline inline) {
        if (this.inlineList == null)
            this.inlineList = new ArrayList<>();
        this.inlineList.add(inline);
    }

    public void addAttachment(MailAttachment attachment) {
        if (this.attachmentList == null)
            this.attachmentList = new ArrayList<>();
        this.attachmentList.add(attachment);
    }
}
