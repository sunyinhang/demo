package com.haiercash.payplatform.service.client;

import com.haiercash.spring.feign.annotation.FeignApi;
import com.haiercash.spring.rest.common.CommonResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-08.
 */
@FeignClient(name = "${app.rest.UAUTH}", path = "/app/uauth")
public interface UauthClient {
    /**
     * @param mobile 手机号, 需加密
     */
    @FeignApi("根据手机号判断是否注册")
    @GetMapping(value = "/isRegister")
    CommonResponse<Map> isRegister(@RequestParam("mobile") String mobile);

    /**
     * @param userId 手机号或 userId, 需加密
     */
    @FeignApi("根据手机号或 userId 获取 userId")
    @GetMapping(value = "/getUserId")
    CommonResponse<Map> getUserId(@RequestParam("userId") String userId);

    /**
     * @param userId 需加密
     */
    @FeignApi("根据 userId 查询绑定手机号")
    @GetMapping(value = "/getMobile")
    CommonResponse<Map> getMobile(@RequestParam("userId") String userId);

    /**
     * @param externCompanyNo channelNo 加密
     * @param externUid       加密
     * @return
     */
    @FeignApi("根据三方 uid 查询用户信息")
    @GetMapping(value = "/queryUserByExternUid")
    CommonResponse<Map> queryUserByExternUid(@RequestParam("externCompanyNo") String externCompanyNo,
                                             @RequestParam("externUid") String externUid);

    @FeignApi("注册第三方用户")
    @PostMapping("/saveUserByExternUid")
    CommonResponse<String> saveUserByExternUid(@RequestBody Map<String, Object> params);

    @FeignApi("不验证并绑定第三方用户")
    @PostMapping("/unvalidateAndBindUserByExternUid")
    CommonResponse<Map> unvalidateAndBindUserByExternUid(@RequestBody Map<String, Object> params);
}
