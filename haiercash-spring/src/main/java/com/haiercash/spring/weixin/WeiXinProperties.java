package com.haiercash.spring.weixin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.weixin")
public class WeiXinProperties {
    private String appid;
    private String secret;
    private String tokenUrl;
    private String ticketUrl;
}
