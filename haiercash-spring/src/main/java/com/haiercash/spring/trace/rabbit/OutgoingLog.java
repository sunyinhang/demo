package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.lang.ThrowableUtils;
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

    public static Map<String, Object> writeBeginLog(Message message, String exchange, String routingKey) {
        String msgId = TraceUtils.getMsgId(message);
        Map<String, Object> log = new LinkedHashMap<>();
        if (StringUtils.isNotEmpty(msgId))
            log.put("msgId", msgId);
        log.put("exchange", exchange);
        log.put("routingKey", routingKey);
        log.put("messageHeaders", TraceUtils.getHeaders(message));
        log.put("messageBody", TraceUtils.getBody(message));
        return log;
    }

    public static void writeEndLog(Map<String, Object> log, long tookMs) {
        log.put("result", "成功");
        log.put("took", tookMs);
        logger.info("==>Rabbit Produce: " + log);
    }

    public static void writeErrorLog(Map<String, Object> log, Exception e, long tookMs) {
        log.put("error", ThrowableUtils.getMessage(e));
        log.put("took", tookMs);
        logger.error("==>Rabbit Produce Error: " + log);
    }
}
