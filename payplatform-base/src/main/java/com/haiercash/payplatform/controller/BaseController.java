package com.haiercash.payplatform.controller;

import com.bestvike.lang.StringUtils;
import com.bestvike.lang.ThrowableUtils;
import com.haiercash.commons.controller.AbstractController;
import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.rest.common.CommonResponse;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.RestUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

public class BaseController extends AbstractController {
    public BaseController(String moduleNo) {
        super(moduleNo);
    }

    protected Map<String, Object> fail(String retFlag, String retMsg) {
        return RestUtil.fail(ConstUtil.APP_CODE + this.getModuleNo() + retFlag, retMsg);
    }

    protected Map<String, Object> fail(String retFlag, String retMsg, Object result) {
        Map<String, Object> resultMap = RestUtil.fail(ConstUtil.APP_CODE + this.getModuleNo() + retFlag, retMsg);
        resultMap.put("body", result);
        return resultMap;
    }

    protected Map<String, Object> success() {
        return RestUtil.success();
    }

    protected Map<String, Object> success(Object result) {
        return RestUtil.success(result);
    }

    public final String getModuleNo() {
        return this.module == null ? StringUtils.EMPTY : this.module;
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

    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse> handleBusinessException(BusinessException e) {
        CommonResponse response = CommonResponse.create(e.getRetFlag(), e.getRetMsg());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleException(Exception e) {
        this.logger.error(ThrowableUtils.getString(e));
        CommonResponse response = CommonResponse.create(ConstUtil.APP_CODE + this.getModuleNo() + ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
