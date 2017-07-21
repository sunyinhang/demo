package com.haiercash.common.service;

import com.haiercash.common.util.ConstUtil;
import com.haiercash.common.util.ThreadLocalFactory;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseService {
    public Log logger = LogFactory.getLog(getClass());

    @Autowired
    protected RestTemplate restTemplate;

    private String moduleNo;

    @Value("${common.address.gateUrl}")
    private String gateUrl;

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
        return "";
        /*if (this.httpServletRequest != null) {
	      return this.httpServletRequest.getHeader("access_token");
	    }
	    return null;*/
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
        ThreadLocal threadLocal = ThreadLocalFactory.getThreadLocal();
        Map<String, Object> threadMap = (Map<String, Object>) threadLocal.get();
        Object channel = threadMap.get("channel");
        return StringUtils.isEmpty(channel) ? "" : (String)channel;
    }

    protected String getChannelNo() {
        ThreadLocal threadLocal = ThreadLocalFactory.getThreadLocal();
        Map<String, Object> threadMap = (Map<String, Object>) threadLocal.get();
        Object channelNo = threadMap.get("channelNo");
        return StringUtils.isEmpty(channelNo) ? "" : (String)channelNo;
    }
}
