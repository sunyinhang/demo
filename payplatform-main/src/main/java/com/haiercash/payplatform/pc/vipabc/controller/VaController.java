package com.haiercash.payplatform.pc.vipabc.controller;

import com.haiercash.spring.controller.BaseController;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/12/25.
 */
@RestController
public class VaController extends BaseController {
    private static final String MODULE_NO = "04";

    public VaController() {
        super(MODULE_NO);
    }

}
