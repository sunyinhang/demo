package com.haiercash.acquirer.controller;

import com.haiercash.commons.rest.cmis.CmisEurekaRest;
import com.haiercash.commons.rest.cmis.CmisResponse;
import com.haiercash.commons.rest.cmis.CmisRestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lihua on 2017/2/28.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoCmisController extends BaseCmisController {
    @Autowired
    private CmisRestUtil cmisRestUtil;
    @Autowired
    private CmisEurekaRest cmisEurekaRest;

    public DemoCmisController() {
        super("FF");
    }

    @RequestMapping("/cmis/1")
    public CmisResponse cmis1() {
        Object result = cmisRestUtil.parseResponseBody(cmisEurekaRest.post(cmisRestUtil.buildRequest("100074")));
        return super.success(result);
    }
}
