package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.alipay")
public class AlipayConfig {
    private String url;
    private String appId;
    private String appPrivateKey;//app 的私钥,公钥配置到支付宝官方的管理界面 https://openhome.alipay.com/platform/detailApp.htm?appId=${appId}&tab=appDetail
    private String alipayPublicKey;//支付宝的公钥,从支付宝官方的管理界面获取
}
