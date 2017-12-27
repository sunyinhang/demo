package com.haiercash.spring.service;

import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.util.RestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class BaseService {
    protected final Log logger = LogFactory.getLog(this.getClass());

    protected final Map<String, Object> fail(String retFlag, String retMsg) {
        return RestUtil.fail(retFlag, retMsg);
    }

    protected final Map<String, Object> success() {
        return RestUtil.success();
    }

    protected final Map<String, Object> success(Object result) {
        return RestUtil.success(result);
    }

    protected final String getModuleNo() {
        return ThreadContext.getExecutingModuleNo();
    }

    protected final String getToken() {
        return ThreadContext.getToken();
    }

    protected final String getChannel() {
        return ThreadContext.getChannel();
    }

    protected final String getChannelNo() {
        return ThreadContext.getChannelNo();
    }
}
