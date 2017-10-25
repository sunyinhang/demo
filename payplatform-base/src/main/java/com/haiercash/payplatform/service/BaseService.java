package com.haiercash.payplatform.service;

import com.haiercash.commons.service.AbstractService;
import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.utils.RestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

public class BaseService extends AbstractService {
    public Log logger = LogFactory.getLog(getClass());

    @Value("${app.other.outplatform_url}")
    protected String outplatUrl;

    @Override
    protected String getGateUrl() {
        return this.gateUrl;
    }

    protected Map<String, Object> fail(String retFlag, String retMsg) {
        return RestUtil.fail(retFlag, retMsg);
    }

    protected Map<String, Object> success() {
        return RestUtil.success();
    }

    protected Map<String, Object> success(Object result) {
        return RestUtil.success(result);
    }

    public final String getModuleNo() {
        return ThreadContext.getExecutingModuleNo();
    }

    @Override
    protected final String getToken() {
        return ThreadContext.getToken();
    }

    @Override
    protected final String getChannel() {
        return ThreadContext.getChannel();
    }

    @Override
    protected final String getChannelNo() {
        return ThreadContext.getChannelNo();
    }
}
