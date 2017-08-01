package com.haiercash.acquirer.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haiercash.commons.mybatis.QueryParam;
import com.haiercash.commons.rest.cmis.CmisEurekaRest;
import com.haiercash.commons.rest.cmis.CmisRestUtil;
import com.haiercash.commons.service.CommonService;
import com.haiercash.payplatform.common.controller.BasePageController;

/**
 * Created by lihua on 2017/2/28.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoPageController extends BasePageController {
    @Autowired
    private CmisRestUtil cmisRestUtil;
    @Autowired
    private CmisEurekaRest cmisEurekaRest;
    @Autowired
    private CommonService commonService;

    @Autowired
    LcApplService lcAppleService;

    public DemoPageController() {
        super("FF");
    }

    @RequestMapping("/cmis/3")
    public Object cmis1() {
        Map<String, Object> map = lcAppleService.select(1303845L);
        System.out.println(map);

        Object result = cmisRestUtil.parseResponseBody(cmisEurekaRest.post(cmisRestUtil.buildRequest("100074")));
        return result;
    }

    @RequestMapping("/cmis/11")
    public Object cmis11() {
        Map<String, Object> map = lcAppleService.select(1303845L);
        return map;
    }

    @RequestMapping("/cmis/12")
    public Object cmis12() {
        QueryParam queryParam = new QueryParam();
        queryParam.setEntityName("lcAppl");
        return commonService.selectByFilter(queryParam);
    }
}
