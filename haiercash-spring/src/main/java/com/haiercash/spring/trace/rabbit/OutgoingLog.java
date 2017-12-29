package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.trace.TraceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class OutgoingLog {
    private static final Log logger = LogFactory.getLog(OutgoingLog.class);

    public static Map<String, Object> writeRequestLog(Message message, String exchange, String routingKey) {
        String msgID = TraceUtils.getMsgID(message);
        Map<String, Object> log = new LinkedHashMap<>();
        if (StringUtils.isNotEmpty(msgID))
            log.put("msgID", msgID);
        log.put("exchange", exchange);
        log.put("routingKey", routingKey);
        log.put("messageHeaders", TraceUtils.getHeaders(message));
        log.put("messageBody", TraceUtils.getBody(message));
        return log;
    }

    public static void writeResponseLog(Map<String, Object> log, long tookMs) {
        log.put("result", "成功");
        log.put("took", tookMs);
        logger.info("==>Rabbit Produce: " + JsonSerializer.serialize(log));
    }

    public static void writeErrorLog(Map<String, Object> log, Exception e, long tookMs) {
        String msg = ThrowableUtils.getMessage(e);
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error("==>Rabbit Produce Error: " + JsonSerializer.serialize(log));
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
