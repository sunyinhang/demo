package com.haiercash.payplatform.service.client;

import com.haiercash.spring.feign.annotation.FeignApi;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-23.
 */
@FeignClient(name = "${app.rest.OUTREACHPLATFORM}", path = "/Outreachplatform")
public interface OutreachClient {
    @FeignApi("外联四要素或三要素认证")
    @PostMapping(value = "/api/chinaPay/identifyByFlag")
    Map<String, Object> identifyByFlag(@RequestBody Map<String, Object> params);
}
