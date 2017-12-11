package com.haiercash.spring.mail.service;

import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.mail.MailProperties;
import com.haiercash.spring.mail.MailUtils;
import com.haiercash.spring.mail.core.Mail;
import com.haiercash.spring.rabbit.RabbitUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 许崇雷 on 2017-11-23.
 */
@RestController
@EnableConfigurationProperties(MailProperties.class)
public class MailController extends BaseController {
    public static final String API_SEND_MAIL = "/api/mail/send";

    @Autowired
    private MailProperties properties;

    public MailController() {
        super("00");
    }

    @PostMapping(value = API_SEND_MAIL)
    public IResponse sendMail(@RequestBody Mail mail) {
        boolean async = this.properties.getAsync() != null && this.properties.getAsync();
        if (async)
            RabbitUtils.convertAndSend(this.properties.getRabbit(), mail);
        else
            MailUtils.send(mail);
        return CommonResponse.success();
    }
}
