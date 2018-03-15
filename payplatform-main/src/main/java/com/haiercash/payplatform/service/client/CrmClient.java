package com.haiercash.payplatform.service.client;

import com.haiercash.spring.feign.annotation.FeignApi;
import com.haiercash.spring.rest.common.CommonResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-23.
 */
@FeignClient(name = "${app.rest.CRM}", path = "/app/crm")
public interface CrmClient {
    @FeignApi("编辑外部认证系统客户标识号")
    @PostMapping("/cust/editExternCompanyNo")
    CommonResponse<Map> editExternCompanyNo(@RequestBody Map<String, Object> params);

    @FeignApi("待还金额还款明细查询")
    @PostMapping("/apporder/queryApplAmtAndRepayByloanNo")
    CommonResponse<Map> queryApplAmtAndRepayByloanNo(@RequestBody Map<String, Object> params);
}
