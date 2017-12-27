package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.trace.TraceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class OutgoingLog {
    private static final Log logger = LogFactory.getLog(OutgoingLog.class);

    public static Map<String, Object> writeRequestLog(Message message, String exchange, String routingKey) {
        String traceID = ThreadContext.getTraceID();
        MessageProperties properties = message.getMessageProperties();
        String msgID = properties.getMessageId();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        if (StringUtils.isNotEmpty(msgID))
            log.put("msgID", msgID);
        log.put("exchange", exchange);
        log.put("routingKey", routingKey);
        log.put("messageHeaders", TraceUtils.getHeaders(properties));
        log.put("messageBody", TraceUtils.getBody(message.getBody(), properties.getContentEncoding()));
        return log;
    }

    public static void writeResponseLog(Map<String, Object> log, long tookMs) {
        log.put("result", "成功");
        log.put("took", tookMs);
        logger.info(String.format("[%s] ==>Rabbit Produce: %s", ThreadContext.getTraceID(), JsonSerializer.serialize(log)));
    }

    public static void writeErrorLog(Map<String, Object> log, Exception e, long tookMs) {
        String msg = ThrowableUtils.getMessage(e);
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error(String.format("[%s] ==>Rabbit Produce Error: %s", ThreadContext.getTraceID(), JsonSerializer.serialize(log)));
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
