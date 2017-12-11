package com.haiercash.spring.mail;

import com.haiercash.spring.rabbit.RabbitInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Data
@ConfigurationProperties(prefix = "spring.mail")
public final class MailProperties {
    private Boolean forward;//是否转发
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean async;//作为转发中心时,是否异步发送,采用 mq 堆积
    private RabbitInfo rabbit;//异步发送的 mq
}
