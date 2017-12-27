package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.rabbit.exception.ConsumeDisabledException;
import com.haiercash.spring.trace.TraceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class IncomingLog {
    private static final Log logger = LogFactory.getLog(IncomingLog.class);

    public static void writeRequestLog(Message message) {
        String traceID = ThreadContext.getTraceID();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("msgID", TraceUtils.getMsgID(message));
        log.put("messageHeaders", TraceUtils.getHeaders(message));
        log.put("messageBody", TraceUtils.getBody(message));
        logger.info(String.format("[%s] ==>Rabbit Consume Begin: %s", traceID, JsonSerializer.serialize(log)));
    }

    public static void writeResponseLog(Message message, long tookMs) {
        String traceID = ThreadContext.getTraceID();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("msgID", TraceUtils.getMsgID(message));
        log.put("result", "消费成功");
        log.put("took", tookMs);
        logger.info(String.format("[%s] ==>Rabbit Consume End: %s", traceID, JsonSerializer.serialize(log)));
    }

    public static void writeDisabled(Message message, long tookMs) {
        String traceID = ThreadContext.getTraceID();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("msgID", TraceUtils.getMsgID(message));
        log.put("result", ConsumeDisabledException.MESSAGE);
        log.put("took", tookMs);
        logger.warn(String.format("[%s] ==>Rabbit Consume Disabled: %s", traceID, JsonSerializer.serialize(log)));
    }

    public static void writeError(Message message, Exception e, long tookMs) {
        String traceID = ThreadContext.getTraceID();
        String msg = ThrowableUtils.getString(e);
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("msgID", TraceUtils.getMsgID(message));
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error(String.format("[%s] ==>Rabbit Consume Error: %s", traceID, JsonSerializer.serialize(log)));
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
