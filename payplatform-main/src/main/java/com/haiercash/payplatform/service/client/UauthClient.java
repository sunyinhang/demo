package com.haiercash.payplatform.service.client;

import com.haiercash.spring.feign.annotation.FeignApi;
import com.haiercash.spring.rest.common.CommonResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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
}
