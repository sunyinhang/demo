package com.haiercash.payplatform.config;

import com.haiercash.core.time.DateUtils;
import com.haiercash.core.time.Time;
import com.haiercash.core.time.TimeRange;
import com.haiercash.core.time.TimeSpan;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.alipay")
public class AlipayConfig {
    public static final String APPL_CARD_NO = "0000000000000000000";//支付宝默认放款卡号
    public static final String REPAY_APPL_CARD_NO = "6225880121940000000";//支付宝默认还款卡号 TODO 测试用,需要改为 19 个 0
    public static final Time ALLOW_PAY_SPAN_BEGIN = new Time(6, 0, 0);//允许支付开始
    public static final Time ALLOW_PAY_SPAN_END = new Time(21, 0, 0);//允许支付结束
    public static final TimeRange ALLOW_PAY_SPAN = new TimeRange(ALLOW_PAY_SPAN_BEGIN, ALLOW_PAY_SPAN_END);//允许支付时间段
    public static final TimeSpan PAY_TIMEOUT = new TimeSpan(0, 10, 0);//支付超时时间 10 分钟

    private String url;
    private String appId;
    private String appPrivateKey;//app 的私钥,公钥配置到支付宝官方的管理界面 https://openhome.alipay.com/platform/detailApp.htm?appId=${appId}&tab=appDetail
    private String alipayPublicKey;//支付宝的公钥,从支付宝官方的管理界面获取
    private Integer fuwuMinScore;//生活号最小分数
    private String wapPayReturnUrl;//支付结果回调页面,H5 提供
    private String wapPayNotifyUrl;//订单状态通知接口,支付网关提供
    private String wapPaySubject;//支付宝 wapPay 时的 wapPaySubject,商品名称

    //获取最终支付时间
    public static Date getLastPayTime(Date payNoCreateTime) {
        Date timeExpire = DateUtils.add(payNoCreateTime, AlipayConfig.PAY_TIMEOUT);//创建时间+10分钟
        Date timeExpireForce = DateUtils.set(payNoCreateTime, AlipayConfig.ALLOW_PAY_SPAN_END);//最晚支付 21:00
        if (timeExpire.compareTo(timeExpireForce) > 0)
            timeExpire = timeExpireForce;
        return timeExpire;
    }
}
