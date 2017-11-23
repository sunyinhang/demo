package com.haiercash.spring.mail;

import com.haiercash.spring.mail.core.MailSendMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Data
@ConfigurationProperties(prefix = "spring.mail")
public final class MailProperties {
    private String mode = MailSendMode.DIRECT;//发送方式
    private String host;
    private Integer port;
    private String username;
    private String password;
}
