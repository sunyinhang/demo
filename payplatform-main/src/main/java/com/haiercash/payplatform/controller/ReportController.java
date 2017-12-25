package com.haiercash.payplatform.controller;

import com.haiercash.spring.controller.BaseController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 命令行控制器
 * Created by 许崇雷 on 2017-12-25.
 */
@RestController
public class ReportController extends BaseController {
    private static String MODULE_NO = "01";

    public ReportController() {
        super(MODULE_NO);
    }

    @RequestMapping(value = "/api/payment/report", method = RequestMethod.POST)
    public void report(@RequestBody String log) {
        logger.warn("客户端日志: " + log);
    }
}
