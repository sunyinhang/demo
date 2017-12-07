package com.haiercash.spring.trace.scheduling;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 许崇雷 on 2017-12-07.
 */
public final class ErrorHandler {
    private static final Log logger = LogFactory.getLog(ErrorHandler.class);

    private ErrorHandler() {
    }

    public static void handleException(Exception e) {
        String msg = ThrowableUtils.getString(e);
        logger.error(msg);
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
