package com.haiercash.spring.trace.feign;

import com.haiercash.spring.feign.core.DelegateMethodHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-10.
 */
public final class OutgoingLog {
    private static final Log logger = LogFactory.getLog(OutgoingLog.class);

    public static Map<String, Object> writeBeginLog(DelegateMethodHandler handler) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("name", handler.getName());
        logger.info("==>Call Feign Api Begin: " + log);
        return log;
    }

    public static void writeEndLog(Map<String, Object> log, long tookMs) {
        log.put("took", tookMs);
        logger.info("==>Call Feign Api End: " + log);
    }
}
