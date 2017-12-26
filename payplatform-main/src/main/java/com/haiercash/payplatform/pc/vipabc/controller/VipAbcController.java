package com.haiercash.payplatform.pc.vipabc.controller;

import com.haiercash.payplatform.pc.vipabc.service.VipAbcService;
import com.haiercash.spring.controller.BaseController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class VipAbcController extends BaseController {
    private static String MODULE_NO = "03";
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private VipAbcService vipAbcService;

    public VipAbcController() {
        super(MODULE_NO);
    }

    @RequestMapping(value = "/api/payment/shunguang/getIdCardInfo", method = RequestMethod.POST)
    public Map<String, Object> getIdCardInfo(@RequestBody Map<String, Object> map) {
        return vipAbcService.getIdCardInfo(map);
    }
}
