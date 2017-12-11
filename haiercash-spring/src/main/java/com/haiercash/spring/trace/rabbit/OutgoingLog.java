package com.haiercash.spring.trace.rabbit;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.trace.TraceConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class OutgoingLog {
    private static final String INVOKE_BEGIN = "----------------Rabbit 生产----------------";
    private static final String INVOKE_SPLIT = "<<-------->>";
    private static final String INVOKE___END = "----------------------------------------";
    private static final Log logger = LogFactory.getLog(OutgoingLog.class);

    public static StringBuilder writeRequestLog(Message message, String exchange, String routingKey) {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(INVOKE_BEGIN).append(Environment.NewLine);
        MessageProperties properties = message.getMessageProperties();
        builder.append("[").append(ThreadContext.getTraceID()).append("]");
        String msgId = properties.getMessageId();
        if (StringUtils.isNotEmpty(msgId))
            builder.append(" ").append(msgId).append(Environment.NewLine);
        //
        builder.append("Exchange:").append(Environment.NewLine);
        builder.append("    ").append(exchange).append(Environment.NewLine);
        //
        builder.append("Routing Key:").append(Environment.NewLine);
        builder.append("    ").append(routingKey).append(Environment.NewLine);
        //
        builder.append("Message Headers:").append(Environment.NewLine);
        builder.append("    content-type:").append(properties.getContentType()).append(Environment.NewLine);
        builder.append("    content-encoding:").append(properties.getContentEncoding()).append(Environment.NewLine);
        //
        builder.append("Message Body:").append(Environment.NewLine);
        builder.append("    ").append(getBody(message.getBody(), properties.getContentEncoding())).append(Environment.NewLine);
        builder.append(INVOKE_SPLIT).append(Environment.NewLine);
        return builder;
    }

    public static void writeResponseLog(StringBuilder builder, long tookMs) {
        builder.append("Result:").append(Environment.NewLine);
        builder.append("    成功").append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(INVOKE___END);
        logger.info(builder.toString());
    }

    @SuppressWarnings("Duplicates")
    public static void writeErrorLog(StringBuilder builder, Exception e, long tookMs) {
        String msg = ThrowableUtils.getMessage(e);
        builder.append("Error:").append(Environment.NewLine);
        builder.append("    ").append(msg).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(INVOKE___END);
        logger.error(builder.toString());
    }

    private static String getBody(byte[] body, String encoding) {
        encoding = Convert.defaultString(encoding, TraceConfig.DEFAULT_CHARSET_NAME);
        try {
            return new String(body, encoding);
        } catch (Exception e) {
            return TraceConfig.BODY_PARSE_FAIL;
        }
    }
}
