package com.haiercash.spring.mail;

import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.mail.core.Mail;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.utils.ConstUtil;
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
    public IResponse doPost(@RequestBody Mail mail) {
        try {
            MailUtils.send(mail);
            return CommonResponse.success();
        } catch (Exception e) {
            this.logger.warn("发送邮件失败", e);
            return CommonResponse.create(ConstUtil.ERROR_CODE, "邮件发送失败");
        }
    }
}
