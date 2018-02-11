package com.haiercash.payplatform.pc.alipay.controller;

import com.alipay.api.AlipayApiException;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.pc.alipay.service.AlipayFuwuService;
import com.haiercash.payplatform.service.OCRIdentityService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Autowired
    private OCRIdentityService ocrIdentityService;

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
    @GetMapping("/api/payment/alipay/fuwu/validUser")
    public IResponse<Map> validUser(@RequestParam Map<String, String> params) throws AlipayApiException, IOException {
        String authCode = params.get("auth_code");
        this.assertAuthCode(authCode);
        this.assertChannelNo();
        this.assertToken();
        return alipayFuwuService.validUser(authCode);
    }

    //ocr
    @PostMapping("/api/payment/alipay/fuwu/ocrIdentity")
    public IResponse<Map> ocrIdentity(@RequestBody MultipartFile identityCard) throws Exception {
        return ocrIdentityService.ocrIdentity(OCRIdentityService.OcrPathType.ByIdNo, identityCard);
    }

    //实名认证
    @PostMapping("/api/payment/alipay/fuwu/realAuthentication")
    public IResponse<Map> realAuthentication(@RequestBody Map<String, Object> map) throws IOException {
        this.assertChannelNo();
        this.assertToken();
        String verifyNo = Convert.toString(map.get("verifyNo"));
        if (StringUtils.isEmpty(verifyNo))
            throw new BusinessException(ConstUtil.ERROR_CODE, "请输入验证码");
        String phone = Convert.toString("mobile");
        if (StringUtils.isEmpty(phone))
            throw new BusinessException(ConstUtil.ERROR_CODE, "手机号不能为空");
        return alipayFuwuService.realAuthentication(map);
    }
}
