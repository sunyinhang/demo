package com.haiercash.spring.controller;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.trace.rest.ErrorHandler;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.RestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

public class BaseController {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final String moduleNo;

    public BaseController(String moduleNo) {
        this.moduleNo = moduleNo;
    }

    protected final Map<String, Object> fail(String retFlag, String retMsg) {
        return RestUtil.fail(retFlag, retMsg);
    }

    protected final Map<String, Object> success() {
        return RestUtil.success();
    }

    protected final Map<String, Object> success(Object result) {
        return RestUtil.success(result);
    }

    public final String getModuleNo() {
        return this.moduleNo == null ? StringUtils.EMPTY : this.moduleNo;
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

    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public final ResponseEntity<CommonResponse> handleBusinessException(BusinessException e) {
        return ErrorHandler.handleBusinessException(e);
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public final ResponseEntity<CommonResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ErrorHandler.handleMissingServletRequestParameterException(e);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<CommonResponse> handleException(Exception e) {
        return ErrorHandler.handleException(e);
    }
}
