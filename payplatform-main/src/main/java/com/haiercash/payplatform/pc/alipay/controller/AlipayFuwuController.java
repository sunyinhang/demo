package com.haiercash.payplatform.pc.alipay.controller;

import com.alipay.api.AlipayApiException;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.pc.alipay.service.AlipayFuwuService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@RestController
public class AlipayFuwuController extends BaseController {
    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private AlipayFuwuService alipayFuwuService;

    public AlipayFuwuController() {
        super("60");
    }

    private void assertAppId(String appId) {
        if (!Objects.equals(appId, alipayConfig.getAppId()))
            throw new BusinessException(ConstUtil.ERROR_CODE, "错误的 appId");
    }

    private void assertAuthCode(String authCode) {
        if (StringUtils.isEmpty(authCode))
            throw new BusinessException(ConstUtil.ERROR_CODE, "authCode 不能为空");
    }

    private void assertChannelNo() {
        if (StringUtils.isEmpty(this.getChannelNo()))
            throw new BusinessException(ConstUtil.ERROR_CODE, "渠道号不能为空");
    }

    private void assertToken() {
        if (StringUtils.isEmpty(this.getToken()))
            throw new BusinessException(ConstUtil.ERROR_CODE, "令牌失效请重新登录");
    }


    //联合登陆
    @PostMapping("/api/payment/alipay/fuwu/login")
    public IResponse<Map> login(@RequestBody Map<String, String> params) throws AlipayApiException {
        String appId = params.get("appId");
        String authCode = params.get("authCode");
        this.assertAppId(appId);
        this.assertAuthCode(authCode);
        this.assertChannelNo();
        return alipayFuwuService.login(authCode);
    }

    //授权后验证用户
    @PostMapping("/api/payment/alipay/fuwu/validUser")
    public IResponse<Map> validUser(@RequestBody Map<String, String> params) throws AlipayApiException {
        String appId = params.get("appId");
        String authCode = params.get("authCode");
        this.assertAppId(appId);
        this.assertAuthCode(authCode);
        this.assertChannelNo();
        this.assertToken();
        return alipayFuwuService.validUser(authCode);
    }

    @PostMapping("/api/payment/alipay/fuwu/realAuthentication")
    public IResponse<Map> realAuthentication(Map<String, Object> map) {
        this.assertChannelNo();
        this.assertToken();
        return alipayFuwuService.realAuthentication(map);
    }


}
