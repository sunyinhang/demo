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
 * Created by 许崇雷 on 2018-02-23.
 */
@FeignClient(name = "${app.rest.APPSERVER}", path = "/app/appserver")
public interface AppServerClient {
    @FeignApi("校验短信验证码")
    @PostMapping("/smsVerify")
    CommonResponse<Map> smsVerify(@RequestBody Map<String, Object> body);

    @FeignApi("查询额度状态")
    @GetMapping(value = "/validate/checkEdAppl")
    CommonResponse<Map> checkEdAppl(@RequestParam("channel") String channel,
                                    @RequestParam("channelNo") String channelNo,
                                    @RequestParam("userId") String userId);

    @FeignApi("查询用户实名信息")
    @GetMapping(value = "/crm/cust/queryPerCustInfo")
    CommonResponse<Map> queryPerCustInfo(@RequestParam("channel") String channel,
                                         @RequestParam("channelNo") String channelNo,
                                         @RequestParam("userId") String userId);
}
