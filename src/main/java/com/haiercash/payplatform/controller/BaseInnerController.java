package com.haiercash.payplatform.controller;

import com.haiercash.commons.controller.AbstractController;
import com.haiercash.commons.rest.inner.InnerResponse;
import com.haiercash.commons.rest.inner.InnerResponseError;
import com.haiercash.commons.rest.inner.InnerRestUtil;
import com.haiercash.commons.support.ServiceException;
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

public class BaseInnerController extends AbstractController {
    @Autowired
    private InnerRestUtil innerRestUtil;

    public BaseInnerController(String moduleNo) {
        super(moduleNo);
    }

    public InnerResponse success() {
        return innerRestUtil.buildResponse(null);
    }

    public InnerResponse success(Object body) {
        return innerRestUtil.buildResponse(body);
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
