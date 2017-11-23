package com.haiercash.spring.mail;

import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.mail.core.Mail;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 许崇雷 on 2017-11-23.
 */
@RestController
public class MailController extends BaseController {
    public MailController() {
        super("00");
    }

    @PostMapping(value = "/api/mail/send")
    public IResponse sendMail(@RequestBody Mail mail) {
        MailUtils.send(mail);
        return CommonResponse.success();
    }
}
