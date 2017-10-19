package com.haiercash.payplatform.service;

import com.haiercash.commons.service.AbstractService;
import com.haiercash.payplatform.servlet.RequestContext;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.RestUtil;
import com.haiercash.payplatform.utils.ResultHead;
import org.apache.commons.lang3.StringUtils;
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

    public BaseService() {
    }

    @Override
    protected String getGateUrl() {
        return this.gateUrl;
    }

    protected Map<String, Object> success() {
        return RestUtil.success();
    }

    protected Map<String, Object> success(String retMsg) {
        Map<String, Object> returnMap = RestUtil.success();
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

    protected String getModuleNo() {
        return RequestContext.exists()
                ? RequestContext.data().getEntryModuleNo()
                : StringUtils.EMPTY;
    }

    @Override
    protected String getToken() {
        return RequestContext.data().getToken();
    }

    @Override
    protected String getChannel() {
        return RequestContext.data().getChannel();
    }

    @Override
    protected String getChannelNo() {
        return RequestContext.data().getChannelNo();
    }
}
