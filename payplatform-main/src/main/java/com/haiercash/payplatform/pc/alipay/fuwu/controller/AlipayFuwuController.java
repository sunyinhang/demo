package com.haiercash.payplatform.pc.alipay.fuwu.controller;

import com.haiercash.payplatform.pc.alipay.fuwu.service.AlipayFuwuService;
import com.haiercash.spring.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@RestController
public class AlipayFuwuController extends BaseController {
    @Autowired
    private AlipayFuwuService alipayFuwuService;

    public AlipayFuwuController() {
        super("17");
    }
}
