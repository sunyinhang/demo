package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.rabbit.exception.ConsumeDisabledException;
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

    public static void writeBeginLog(String action, Message message) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("msgId", RabbitLogUtils.getMsgId(message));
        log.put("retry", RabbitLogUtils.getRetry(message));
        log.put("messageHeaders", RabbitLogUtils.getHeaders(message));
        log.put("messageBody", RabbitLogUtils.getBody(message));
        logger.info("==>Rabbit Consume Begin: " + log);
    }

    public static void writeEndLog(String action, Message message, long tookMs) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("msgId", RabbitLogUtils.getMsgId(message));
        log.put("retry", RabbitLogUtils.getRetry(message));
        log.put("result", "消费成功");
        log.put("took", tookMs);
        logger.info("==>Rabbit Consume End: " + log);
    }

    public static void writeDisabledLog(String action, Message message, long tookMs) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("msgId", RabbitLogUtils.getMsgId(message));
        log.put("retry", RabbitLogUtils.getRetry(message));
        log.put("result", ConsumeDisabledException.MESSAGE);
        log.put("took", tookMs);
        logger.warn("==>Rabbit Consume Disabled: " + log);
    }

    public static void writeWarnLog(String action, Message message, int retry, Exception e, long tookMs) {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("msgId", RabbitLogUtils.getMsgId(message));
        log.put("retry", RabbitLogUtils.getRetry(message));
        log.put("result", String.format("消费异常: %s, 将进行第 %d 次重试", ThrowableUtils.getMessage(e), retry));
        log.put("took", tookMs);
        logger.warn("==>Rabbit Consume Warn: " + log);
    }

    public static void writeErrorLog(String action, Message message, Exception e, long tookMs) {
        String msg = ThrowableUtils.getString(e);
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("action", action);
        log.put("msgId", RabbitLogUtils.getMsgId(message));
        log.put("retry", RabbitLogUtils.getRetry(message));
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error("==>Rabbit Consume Error: " + log);
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
