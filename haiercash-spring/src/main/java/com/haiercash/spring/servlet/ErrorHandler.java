package com.haiercash.spring.servlet;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.utils.BusinessException;
import com.haiercash.spring.utils.ConstUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Created by 许崇雷 on 2017-10-25.
 */
public final class ErrorHandler {
    private static final Log logger = LogFactory.getLog(ErrorHandler.class);

    private ErrorHandler() {
    }

    public static String getRetFlag(String retFlag) {
        String moduleNo = Convert.defaultString(ThreadContext.getExecutingModuleNo(), "00");
        return (retFlag == null || retFlag.length() <= 2) ? (ConstUtil.APP_CODE + moduleNo + retFlag) : retFlag;
    }

    public static ResponseEntity<CommonResponse> handleBusinessException(BusinessException e) {
        CommonResponse response = CommonResponse.create(e.getRetFlag(), e.getRetMsg());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<CommonResponse> handleException(Exception e) {
        logger.error(ThrowableUtils.getString(e));
        CommonResponse response = CommonResponse.create(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
