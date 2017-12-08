package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.rabbit.exception.ConsumeDisabledException;
import com.haiercash.spring.trace.TraceConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class IncomingLog {
    private static final String REQ_BEGIN = "----------------Rabbit 消费-------->>>>>>>>";
    private static final String REQ___END = "-------------------------------->>>>>>>>";
    private static final String RES_BEGIN = "<<<<<<<<--------Rabbit 消费完成----------------";
    private static final String COS___END = "<<<<<<<<--------------------------------";
    private static final Log logger = LogFactory.getLog(IncomingLog.class);

    public static void writeRequestLog(Message message) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(REQ_BEGIN).append(Environment.NewLine);
        MessageHeaders headers = message.getHeaders();
        builder.append("[").append(ThreadContext.getTraceID()).append("] ").append(headers.getId()).append(Environment.NewLine);
        //
        builder.append("Message Headers:").append(Environment.NewLine);
        writeHeaders(builder, headers);
        //
        builder.append("Message Body:").append(Environment.NewLine);
        builder.append("    ").append(getBody((byte[]) message.getPayload(), headers)).append(Environment.NewLine);
        builder.append(REQ___END);
        logger.info(builder.toString());
    }

    public static void writeResponseLog(Message message, long tookMs) {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(RES_BEGIN).append(Environment.NewLine);
        MessageHeaders headers = message.getHeaders();
        builder.append("[").append(ThreadContext.getTraceID()).append("] ").append(headers.getId()).append(Environment.NewLine);
        //
        builder.append("Result:").append(Environment.NewLine);
        builder.append("    消费成功").append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(COS___END);
        logger.info(builder.toString());
    }

    public static void writeDisabled(Message message, long tookMs) {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(RES_BEGIN).append(Environment.NewLine);
        MessageHeaders headers = message.getHeaders();
        builder.append("[").append(ThreadContext.getTraceID()).append("] ").append(headers.getId()).append(Environment.NewLine);
        //
        builder.append("Result:").append(Environment.NewLine);
        builder.append("    ").append(ConsumeDisabledException.MESSAGE).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(COS___END);
        logger.warn(builder.toString());
    }

    public static void writeError(Message message, Exception e, long tookMs) {
        String msg = ThrowableUtils.getString(e);
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(RES_BEGIN).append(Environment.NewLine);
        MessageHeaders headers = message.getHeaders();
        builder.append("[").append(ThreadContext.getTraceID()).append("] ").append(headers.getId()).append(Environment.NewLine);
        //
        builder.append("Error:").append(Environment.NewLine);
        builder.append(msg).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(COS___END);
        logger.error(builder.toString());
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }

    private static void writeHeaders(StringBuilder builder, MessageHeaders headers) {
        for (Map.Entry<String, Object> entry : headers.entrySet())
            builder.append("    ").append(entry.getKey()).append(":").append(entry.getValue()).append(Environment.NewLine);
    }

    private static String getBody(byte[] body, MessageHeaders headers) {
        String encoding = headers == null ? TraceConfig.DEFAULT_CHARSET_NAME : Convert.defaultString(headers.get(AmqpHeaders.CONTENT_ENCODING), TraceConfig.DEFAULT_CHARSET_NAME);
        try {
            return new String(body, encoding);
        } catch (Exception e) {
            return TraceConfig.BODY_PARSE_FAIL;
        }
    }
}
