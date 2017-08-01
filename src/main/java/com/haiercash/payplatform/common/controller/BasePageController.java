package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.controller.AbstractController;
import com.haiercash.commons.rest.RestError;
import com.haiercash.commons.rest.RestStatus;
import com.haiercash.commons.support.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 页面请求专用controller，异常时不返回head、body
 */
public class BasePageController extends AbstractController {
    public BasePageController(String moduleNo) {
        super(moduleNo);
    }

    /**
     * Controller参数异常
     * Post、Put、Patch请求@Valid引起
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<RestError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error(e);
        BindingResult errors = e.getBindingResult();
        StringBuilder strBuilder = new StringBuilder();
        for (FieldError fieldError : errors.getFieldErrors()) {
            strBuilder.append(fieldError.getDefaultMessage() + "\n");
        }
        RestError restError = RestError.build(this.appId, this.prefix, this.module, RestStatus.BAD_REQUEST, RestStatus.INTERNAL_SERVER_ERROR.getCode(), RestStatus.INTERNAL_SERVER_ERROR.getMessage(), strBuilder.toString());
        return new ResponseEntity<RestError>(restError, RestStatus.BAD_REQUEST.getStatus());
    }

    /**
     * Controller参数异常
     * Get请求@RequestParam引起
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<RestError> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error(e);
        RestError restError = RestError.build(this.appId, this.prefix, this.module, RestStatus.BAD_REQUEST,
                RestStatus.INTERNAL_SERVER_ERROR.getCode(), RestStatus.INTERNAL_SERVER_ERROR.getMessage(), e.getMessage());
        return new ResponseEntity<RestError>(restError, RestStatus.BAD_REQUEST.getStatus());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ResponseEntity<RestError> handleUsernameNotFoundException(UsernameNotFoundException e) {
        logger.error(e);
        logger.error(e.getCause());
        RestError restError = RestError.build(this.appId, this.prefix, this.module, RestStatus.FORBIDDEN, "username", "用户不存在", e.getMessage());
        return new ResponseEntity<RestError>(restError, RestStatus.FORBIDDEN.getStatus());
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<RestError> handleBusinessException(ServiceException e) {
        logger.error(e);
        logger.error(e.getCause());
        RestError restError = RestError.build(this.appId, this.prefix, this.module, RestStatus.SERVICE_ERROR, e.getId(), e.getMessage(), e.getDebug());
        return new ResponseEntity<RestError>(restError, RestStatus.SERVICE_ERROR.getStatus());
    }

    /**
     * 其他未处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<RestError> handleException(Exception e) {
        logger.error(e);
        logger.error(e.getCause());
        RestError restError = RestError.build(this.appId, this.prefix, this.module, RestStatus.INTERNAL_SERVER_ERROR, RestStatus.INTERNAL_SERVER_ERROR.getCode(), RestStatus.INTERNAL_SERVER_ERROR.getMessage(), e.getMessage());
        return new ResponseEntity<RestError>(restError, RestStatus.INTERNAL_SERVER_ERROR.getStatus());
    }
}
