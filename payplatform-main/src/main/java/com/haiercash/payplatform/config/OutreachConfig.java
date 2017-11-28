package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by yu jianwei on 2017/11/28
 *
 * @Description:
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.outreach")
public class OutreachConfig {
    private String appid;
    private String channelNo;
}
