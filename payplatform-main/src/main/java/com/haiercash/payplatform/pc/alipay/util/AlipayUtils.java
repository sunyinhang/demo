package com.haiercash.payplatform.pc.alipay.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.request.ZhimaCreditScoreBriefGetRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.alipay.api.response.ZhimaCreditScoreBriefGetResponse;
import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@Component
public class AlipayUtils {
    private static final String FORMAT = "json";
    private static final String SIGN_TYPE = "RSA2";
    private static final String CHARSET = CharsetNames.UTF_8;
    private static AlipayConfig alipayConfig;
    @Autowired
    private AlipayConfig alipayConfigInstance;

    private static AlipayClient getDefaultClient() {
        return new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getAppId(), alipayConfig.getAppPrivateKey(), FORMAT, CHARSET, alipayConfig.getAlipayPublicKey(), SIGN_TYPE);
    }

    private static <T extends AlipayResponse> T execute(AlipayRequest<T> request, String authToken) throws AlipayApiException {
        AlipayClient client = getDefaultClient();
        T response = client.execute(request, authToken);
        if (response == null)
            throw new AlipayApiException(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        if (!response.isSuccess())
            throw new AlipayApiException(response.getCode(), response.getMsg());
        return response;
    }

    private static <T extends AlipayResponse> T execute(AlipayRequest<T> request) throws AlipayApiException {
        return execute(request, null);
    }

    private static <T extends AlipayResponse> T pageExecute(AlipayRequest<T> request) throws AlipayApiException {
        AlipayClient client = getDefaultClient();
        T response = client.pageExecute(request);
        if (response == null)
            throw new AlipayApiException(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        if (!response.isSuccess())
            throw new AlipayApiException(response.getCode(), response.getMsg());
        return response;
    }

    //根据 auth_code(一次性) 获取 user_id token refresh_token
    public static AlipayToken getOauthTokenByAuthCode(String authCode) throws AlipayApiException {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(authCode);
        AlipaySystemOauthTokenResponse response = execute(request);
        return new AlipayToken(response);
    }

    //根据 token 获取用户信息
    public static AlipayUserInfoShareResponse getUserInfo(String authToken) throws AlipayApiException {
        AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
        return execute(request, authToken);
    }

    //根据 refresh_token 获取  user_id token refresh_token
    public static AlipayToken refreshOauthToken(String refreshToken) throws AlipayApiException {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("refresh_token");
        request.setCode(refreshToken);
        AlipaySystemOauthTokenResponse response = execute(request);
        return new AlipayToken(response);
    }

    //判断芝麻信用分数是否达标
    public static boolean assertScoreByUserId(String alipayUserId, int score) throws AlipayApiException {
        Map<String, Object> params = new HashMap<>();
        params.put("transaction_id", UUID.randomUUID().toString());
        params.put("product_code", "w1010100000000002733");
        params.put("cert_type", "ALIPAY_USER_ID");
        params.put("cert_no", alipayUserId);
        params.put("admittance_score", score);
        ZhimaCreditScoreBriefGetRequest request = new ZhimaCreditScoreBriefGetRequest();
        request.setBizContent(JsonSerializer.serialize(params));
        ZhimaCreditScoreBriefGetResponse response = execute(request);
        return Objects.equals(response.getIsAdmittance(), "Y");
    }

    //调用支付宝还款接口,返回 html
    public static String wapPay(String outTradeNo, String totalAmount, String subject) throws AlipayApiException {
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", subject);
        bizContent.put("product_code", "QUICK_WAP_WAY");
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setReturnUrl(alipayConfig.getWapPayReturnUrl());
        request.setNotifyUrl(alipayConfig.getWapPayNotifyUrl());
        request.setBizContent(JsonSerializer.serialize(bizContent));
        return pageExecute(request).getBody();
    }

    @PostConstruct
    private void init() {
        alipayConfig = this.alipayConfigInstance;
    }
}
