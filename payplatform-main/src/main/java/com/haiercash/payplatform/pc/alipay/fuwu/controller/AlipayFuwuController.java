package com.haiercash.payplatform.pc.alipay.fuwu.controller;

import com.alipay.api.AlipayApiException;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.pc.alipay.fuwu.service.AlipayFuwuService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@RestController
public class AlipayFuwuController extends BaseController {
    private static final String ERROR_URL = "/error";
    private static final String CHANNEL_NO = "127";
    @Autowired
    private AlipayConfig alipayConfig;


    @Autowired
    private AlipayFuwuService alipayFuwuService;

    public AlipayFuwuController() {
        super("17");
    }

    //配置获取
    @GetMapping("/api/payment/alipay/fuwu/config")
    public IResponse<Map> getConfig() {
        Map<String, Object> body = new HashMap<>();
        body.put("appId", alipayConfig.getAppId());
        body.put("channelNo", CHANNEL_NO);
        return CommonResponse.success(body);
    }

    //联合登陆
    @GetMapping("/api/payment/alipay/fuwu/login")
    public IResponse<Map> login(@RequestParam Map<String, String> params) throws AlipayApiException {
        String appId = params.get("appId");
        if (!Objects.equals(appId, alipayConfig.getAppId()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "错误的 appId");
        String authCode = params.get("auth_code");
        if (StringUtils.isEmpty(authCode))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "auth_code 不能为空");
        String channelNo = this.getChannelNo();
        if (StringUtils.isEmpty(channelNo))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "渠道号不能为空");
        return alipayFuwuService.login(authCode);
    }


    //额度入口
    @GetMapping("/api/payment/alipay/fuwu/creditEntry")
    public IResponse<Map> creditEntry(@RequestParam Map<String, String> params) throws IOException, AlipayApiException {
        String appId = params.get("appId");
        if (!Objects.equals(appId, alipayConfig.getAppId()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "错误的 appId");
        String authCode = params.get("auth_code");
        if (StringUtils.isEmpty(authCode))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "auth_code 不能为空");

        return alipayFuwuService.creditEntry(authCode);
    }
}
