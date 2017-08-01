package com.haiercash.acquirer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haiercash.commons.rest.cmis.CmisEurekaRest;
import com.haiercash.commons.rest.cmis.CmisRestUtil;
import com.haiercash.commons.rest.inner.InnerResponse;
import com.haiercash.payplatform.common.controller.BaseInnerController;

/**
 * Created by lihua on 2017/2/28.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoInnerController extends BaseInnerController {
    @Autowired
    private CmisRestUtil cmisRestUtil;
    @Autowired
    private CmisEurekaRest cmisEurekaRest;

    public DemoInnerController() {
        super("FF");
    }

    @RequestMapping("/cmis/2")
    public InnerResponse cmis1() {
        Object result = cmisRestUtil.parseResponseBody(cmisEurekaRest.post(cmisRestUtil.buildRequest("100074")));
        return super.success(result);
    }
}
