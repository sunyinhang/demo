package com.haiercash.payplatform.pc.vipabc.controller;

import com.haiercash.payplatform.pc.vipabc.service.VaService;
import com.haiercash.spring.controller.BaseController;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
@RestController
public class VaController extends BaseController {
    private static final String MODULE_NO = "04";
    @Autowired
    private VaService vaService;

    public VaController() {
        super(MODULE_NO);
    }

    @RequestMapping(value = "/api/payment/vipabc/queryAppLoanAndGood", method = RequestMethod.POST)
    public Map<String, Object> queryAppLoanAndGood(@RequestBody Map<String, Object> map) throws Exception {
        return vaService.queryAppLoanAndGood(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }

    /**
     * 订单删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/vipabc/vipAbcDeleteOrderInfo", method = RequestMethod.POST)
    public Map<String, Object> vipAbcDeleteOrderInfo(@RequestBody Map<String, Object> map, HttpServletRequest request) throws Exception {
        return vaService.vipAbcDeleteOrderInfo(request, super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }

}
