package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.controller.AbstractController;
import com.haiercash.payplatform.common.filter.RequestContext;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.RestUtil;

import java.util.Map;

public class BaseController extends AbstractController {

    public BaseController(String moduleNo) {
        super(moduleNo);
    }

    protected Map<String, Object> fail(String retFlag, String retMsg) {
        return RestUtil.fail(ConstUtil.APP_CODE + module + retFlag, retMsg);
    }

    protected Map<String, Object> fail(String retFlag, String retMsg, Object result) {
        Map<String, Object> resultMap = RestUtil.fail(ConstUtil.APP_CODE + module + retFlag, retMsg);
        resultMap.put("body", result);
        return resultMap;
    }

    @Override
    protected String getChannel() {
        return RequestContext.data().getChannel();
    }

    protected String getChannelNO() {
        return RequestContext.data().getChannelNo();
    }

    protected boolean isNeedVerify() {
        return RequestContext.data().isNeedVerify();
    }

    protected boolean isExecutedVerify() {
        return RequestContext.data().isExecutedVerify();
    }

    public String getModuleNo() {
        return module;
    }
}
