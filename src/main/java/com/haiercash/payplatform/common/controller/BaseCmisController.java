package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.controller.AbstractController;
import com.haiercash.commons.rest.cmis.CmisResponse;
import com.haiercash.commons.rest.cmis.CmisResponseError;
import com.haiercash.commons.rest.cmis.CmisRestUtil;
import com.haiercash.commons.support.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 信贷接口返回异常
 */
public class BaseCmisController extends AbstractController {
    @Autowired
    private CmisRestUtil cmisRestUtil;

    public BaseCmisController(String moduleNo) {
        super(moduleNo);
    }

    public CmisResponse success(Object body) {
        return cmisRestUtil.buildResponse(body);
    }

    public CmisResponse success(CmisResponse cmisResponse,String serno){
        if(StringUtils.isEmpty(serno)){
            return cmisResponse;
        }else{
            cmisResponse.getResponse().getHead().setSerno(serno);
            return cmisResponse;
        }
    }

    /**
     * Controller参数异常
     * Post、Put、Patch请求@Valid引起
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<CmisResponseError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error(e);
        BindingResult errors = e.getBindingResult();
        StringBuilder strBuilder = new StringBuilder();
        for (FieldError fieldError : errors.getFieldErrors()) {
            strBuilder.append(fieldError.getDefaultMessage() + "\n");
        }
        CmisResponseError cmisResponseError = cmisRestUtil.buildResponse(this.prefix + this.module + cmisRestUtil.ERROR_INTERNAL_CODE, cmisRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<CmisResponseError>(cmisResponseError, HttpStatus.OK);
    }

    /**
     * Controller参数异常
     * Get请求@RequestParam引起
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<CmisResponseError> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error(e);
        CmisResponseError cmisResponseError = cmisRestUtil.buildResponse(this.prefix + this.module + cmisRestUtil.ERROR_INTERNAL_CODE, cmisRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<CmisResponseError>(cmisResponseError, HttpStatus.OK);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ResponseEntity<CmisResponseError> handleUsernameNotFoundException(UsernameNotFoundException e) {
        logger.error(e);
        logger.error(e.getCause());
        CmisResponseError cmisResponseError = cmisRestUtil.buildResponse(this.prefix + this.module + cmisRestUtil.ERROR_INTERNAL_CODE, cmisRestUtil.ERROR_INTERNAL_MSG);
        return new ResponseEntity<CmisResponseError>(cmisResponseError, HttpStatus.OK);
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<CmisResponseError> handleBusinessException(ServiceException e) {
        logger.error(e);
        logger.error(e.getCause());
        CmisResponseError cmisResponseError = cmisRestUtil.buildResponse(this.prefix + this.module + e.getId(), e.getMessage(), String.valueOf(httpSession.getAttribute(httpSession.getId())));
        return new ResponseEntity<CmisResponseError>(cmisResponseError, HttpStatus.OK);
    }

    /**
     * sql异常处理的异常(测试组反馈：字段长度过长时，提示网络通讯异常不合适！)
     */
    @ExceptionHandler(UncategorizedSQLException.class)
    @ResponseBody
    public ResponseEntity<CmisResponseError> handleSUncategorizedSQLException(UncategorizedSQLException e) {
        logger.error(e);
        logger.error(e.getCause());
        CmisResponseError cmisResponseError = cmisRestUtil.buildResponse(this.prefix + this.module + cmisRestUtil.ERROR_INTERNAL_CODE, e.getCause().getMessage(), String.valueOf(httpSession.getAttribute(httpSession.getId())));
        return new ResponseEntity<CmisResponseError>(cmisResponseError, HttpStatus.OK);
    }

    /**
     * 其他未处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<CmisResponseError> handleException(Exception e) {
        logger.error(e);
        logger.error(e.getCause());
        CmisResponseError cmisResponseError = cmisRestUtil.buildResponse(this.prefix + this.module + cmisRestUtil.ERROR_INTERNAL_CODE, cmisRestUtil.ERROR_INTERNAL_MSG, String.valueOf(httpSession.getAttribute(httpSession.getId())));
        return new ResponseEntity<CmisResponseError>(cmisResponseError, HttpStatus.OK);
    }
}
