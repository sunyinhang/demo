package com.haiercash.spring.trace.rest;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

/**
 * Created by 许崇雷 on 2017-10-25.
 */
public final class ErrorHandler {
    private static final String MSG_MISSING_SERVLET_REQUEST_PARAMETER = "缺少必须的参数:";
    private static final Log logger = LogFactory.getLog(ErrorHandler.class);

    private ErrorHandler() {
    }

    public static String getRetFlag(String retFlag) {
        String moduleNo = Convert.defaultString(ThreadContext.getExecutingModuleNo(), "00");
        return (retFlag == null || retFlag.length() <= 2) ? (ConstUtil.APP_CODE + moduleNo + retFlag) : retFlag;
    }

    public static ResponseEntity<CommonResponse> handleBusinessException(BusinessException e) {
        CommonResponse response = CommonResponse.fail(e.getRetFlag(), e.getRetMsg());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<CommonResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        CommonResponse response = CommonResponse.fail(ConstUtil.ERROR_CODE, MSG_MISSING_SERVLET_REQUEST_PARAMETER + e.getParameterName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<CommonResponse> handleException(Exception e) {
        String msg = ThrowableUtils.getString(e);
        logger.error(msg);
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
        CommonResponse response = CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
