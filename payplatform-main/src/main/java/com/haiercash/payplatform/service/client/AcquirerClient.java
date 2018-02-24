package com.haiercash.payplatform.service.client;

import com.haiercash.spring.feign.annotation.FeignApi;
import com.haiercash.spring.rest.acq.AcqRequest;
import com.haiercash.spring.rest.acq.AcqResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-23.
 */
@FeignClient(name = "${app.rest.ACQUIRER}", path = "/api/appl")
public interface AcquirerClient {
    @FeignApi("ACQ-2101提交还款请求")
    @PostMapping("saveZdhkInfo")
    AcqResponse<List<Map>> saveZdhkInfo(@RequestBody AcqRequest body);
}
