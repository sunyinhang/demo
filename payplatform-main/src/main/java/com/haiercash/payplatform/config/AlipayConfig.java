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
    private Integer fuwuMinScore;//生活号最小分数
    private String wapPayReturnUrl;//支付结果回调页面,H5 提供
    private String wapPayNotifyUrl;//订单状态通知接口,支付网关提供
    private String subject;//支付宝 wap pay 时的 subject,商品名称
}
