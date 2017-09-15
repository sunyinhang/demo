package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.controller.AbstractController;
import com.haiercash.commons.rest.inner.InnerResponseError;
import com.haiercash.commons.rest.inner.InnerRestUtil;
import com.haiercash.commons.support.ServiceException;
import com.haiercash.payplatform.common.filter.RequestContext;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

public class BaseController extends AbstractController {
    @Autowired
    private InnerRestUtil innerRestUtil;

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

    protected Map<String, Object> success() {
        return RestUtil.success();
    }

    protected Map<String, Object> success(Object result) {
        return RestUtil.success(result);
    }

    public String getModuleNo() {
        return module;
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

    protected Map<String, Object> initParam(Map<String, Object> paramMap) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        paramMap.put("token", RequestContext.data().getToken());
        paramMap.put("channel", RequestContext.data().getChannel());
        paramMap.put("channelNo", RequestContext.data().getChannelNo());
        return paramMap;
    }

    /**
     * Controller参数异常
     * Post、Put、Patch请求@Valid引起
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<InnerResponseError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error(e);
        BindingResult errors = e.getBindingResult();
        StringBuilder strBuilder = new StringBuilder();
        for (FieldError fieldError : errors.getFieldErrors()) {
            strBuilder.append(fieldError.getDefaultMessage() + "\n");
        }
        InnerResponseError innerResponseError = innerRestUtil.buildResponse(this.prefix + this.module + innerRestUtil.ERROR_INTERNAL_CODE, innerRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<InnerResponseError>(innerResponseError, HttpStatus.OK);
    }

    /**
     * Controller参数异常
     * Get请求@RequestParam引起
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<InnerResponseError> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error(e);
        InnerResponseError innerResponseError = innerRestUtil.buildResponse(this.prefix + this.module + innerRestUtil.ERROR_INTERNAL_CODE, innerRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<InnerResponseError>(innerResponseError, HttpStatus.OK);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ResponseEntity<InnerResponseError> handleUsernameNotFoundException(UsernameNotFoundException e) {
        logger.error(e);
        logger.error(e.getCause());
        InnerResponseError innerResponseError = innerRestUtil.buildResponse(this.prefix + this.module + innerRestUtil.ERROR_INTERNAL_CODE, innerRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<InnerResponseError>(innerResponseError, HttpStatus.OK);
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<InnerResponseError> handleBusinessException(ServiceException e) {
        logger.error(e);
        logger.error(e.getCause());
        InnerResponseError innerResponseError = innerRestUtil.buildResponse(this.prefix + this.module + e.getId(), e.getMessage());
        return new ResponseEntity<InnerResponseError>(innerResponseError, HttpStatus.OK);
    }

    /**
     * 其他未处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<InnerResponseError> handleException(Exception e) {
        logger.error(e);
        logger.error(e.getCause());
        InnerResponseError innerResponseError = innerRestUtil.buildResponse(this.prefix + this.module + innerRestUtil.ERROR_INTERNAL_CODE, innerRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<InnerResponseError>(innerResponseError, HttpStatus.OK);
    }
}
