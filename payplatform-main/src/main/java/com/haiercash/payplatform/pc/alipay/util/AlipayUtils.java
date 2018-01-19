package com.haiercash.payplatform.pc.alipay.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.haiercash.core.io.CharsetNames;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@Component
public class AlipayUtils {
    private static final String FORMAT = "json";
    private static final String SIGN_TYPE = "RSA2";
    private static final String CHARSET = CharsetNames.UTF_8;
    private static final String GATEWAY = "/gateway.do";
    private static final String TOKEN_PREFIX = "ALIPAY:TOKEN:";
    private static AlipayConfig alipayConfig;
    @Autowired
    private AlipayConfig alipayConfigInstance;

    private static String getRedisKey(String userId) {
        return TOKEN_PREFIX + userId;
    }

    private static AlipayClient getDefaultClient() {
        return new DefaultAlipayClient(alipayConfig.getUrl() + GATEWAY, alipayConfig.getAppId(), alipayConfig.getAppPrivateKey(), FORMAT, CHARSET, alipayConfig.getAlipayPublicKey(), SIGN_TYPE);
    }

    private static <T extends AlipayResponse> T execute(AlipayRequest<T> request, String authToken) throws AlipayApiException {
        AlipayClient client = getDefaultClient();
        T response = client.execute(request, authToken);
        if (response == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        if (!response.isSuccess())
            throw new BusinessException(response.getCode(), response.getMsg());
        return response;
    }

    private static <T extends AlipayResponse> T execute(AlipayRequest<T> request) throws AlipayApiException {
        return execute(request, null);
    }

    //根据 auth_code 获取 user_id token refresh_token
    public static AlipayToken getOauthTokenByAuthCode(String authCode) throws AlipayApiException {
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(authCode);
        AlipaySystemOauthTokenResponse response = execute(request);
        AlipayToken token = new AlipayToken(response);
        RedisUtils.set(getRedisKey(response.getUserId()), token);
        return token;
    }

    //根据 user_id 获取 user_id token refresh_token
    public static AlipayToken getOauthTokenByUserId(String userId) throws AlipayApiException {
        AlipayToken token = RedisUtils.get(getRedisKey(userId), AlipayToken.class);
        if (token == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "授权已过期,请重新登录");
        //有效直接返回
        if (token.isValid())
            return token;
        //refresh token 有效,还能抢救
        if (token.isRefreshValid())
            return refreshOauthToken(token.getRefreshToken());
        throw new BusinessException(ConstUtil.ERROR_CODE, "授权已过期,请重新登录");
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
        AlipayToken token = new AlipayToken(response);
        RedisUtils.set(getRedisKey(response.getUserId()), token);
        return token;
    }

    @PostConstruct
    private void init() {
        alipayConfig = this.alipayConfigInstance;
    }
}
