package com.haiercash.spring.weixin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
@ConfigurationProperties(prefix = "app.weixin")
public final class WeiXinProperties {
    private String appid;
    private String secret;
}
