package com.haiercash.spring.trace.scheduling;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-20.
 */
public final class IncomingLog {
    private static final Log logger = LogFactory.getLog(IncomingLog.class);

    public static void writeBeginLog(String action) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        logger.info("==>Scheduled Begin: " + log);
    }

    public static void writeEndLog(String action, long tookMs) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("took", tookMs);
        logger.info("==>Scheduled End: " + log);
    }

    public static void writeErrorLog(String action, Exception e, long tookMs) {
        String msg = ThrowableUtils.getString(e);
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error("==>Scheduled Error: " + log);
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
