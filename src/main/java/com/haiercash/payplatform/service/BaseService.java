package com.haiercash.payplatform.service;

import com.haiercash.commons.service.AbstractService;
import com.haiercash.payplatform.common.filter.RequestContext;
import com.haiercash.payplatform.common.utils.RestUtil;
import com.haiercash.payplatform.common.utils.ResultHead;
import com.haiercash.payplatform.common.utils.ConstUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseService extends AbstractService {
    public Log logger = LogFactory.getLog(getClass());

    @Autowired
    protected RestTemplate restTemplate;

    @Value("${app.other.outplatform_url}")
    protected String outplatUrl;

    private String moduleNo;

//    @Value("${common.address.gateUrl}")
//    private String gateUrl;

    public BaseService() {
    }

    public BaseService(String moduleNo) {
        this.moduleNo = moduleNo;
    }

    public String getModuleNo() {
        return this.moduleNo;
    }

    public void setModuleNo(String moduleNo) {
        this.moduleNo = moduleNo;
    }

    protected String getToken() {
        return RequestContext.data().getToken();
    }

    protected String getGateUrl() {
        return this.gateUrl;
    }

    protected Map<String, Object> success() {
        return RestUtil.success();
    }

    protected Map<String, Object> success(String retMsg) {
        Map<String, Object> returnMap =  RestUtil.success();
        ResultHead head = (ResultHead) returnMap.get("head");
        head.setRetMsg(retMsg);
        returnMap.put("head", head);
        return returnMap;
    }

    protected Map<String, Object> success(Object result) {
        return RestUtil.success(result);
    }

    protected Map<String, Object> success(String retMsg, Object result) {
        Map<String, Object> returnMap = RestUtil.success(result);
        ResultHead head = (ResultHead) returnMap.get("head");
        head.setRetMsg(retMsg);
        returnMap.put("head", head);
        return returnMap;
    }

    protected boolean isSuccess(Map<String, Object> resultMap) {
        return RestUtil.isSuccess(resultMap);
    }

    protected Map<String, Object> fail(String retFlag, String retMsg) {
        return RestUtil.fail(ConstUtil.APP_CODE + this.getModuleNo() + retFlag, retMsg);
    }

    protected Map<String, Object> fail(String retFlag, String retMsg, Object result) {
        Map<String, Object> resultMap = RestUtil.fail(ConstUtil.APP_CODE + this.getModuleNo() + retFlag, retMsg);
        resultMap.put("body", result);
        return resultMap;
    }
    protected String getChannel() {
        return RequestContext.data().getChannel();
    }

    protected String getChannelNo() {
        return RequestContext.data().getChannelNo();
    }

}
