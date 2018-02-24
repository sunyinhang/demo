package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 赵先鲁 on 2018/2/6.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.vipabv")
public class VipabcConfig {
    private String goodsBrand;
    private String goodsKind;
    private String tutorabc;
    private String merchantCode;
    private String storeNo;
}
