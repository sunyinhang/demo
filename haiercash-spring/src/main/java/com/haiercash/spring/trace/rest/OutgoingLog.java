package com.haiercash.spring.trace.rest;

import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.spring.client.ClientRequestWrapper;
import com.haiercash.spring.client.ClientResponseWrapper;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.trace.TraceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class OutgoingLog {
    private static final Log logger = LogFactory.getLog(OutgoingLog.class);

    public static Map<String, Object> writeRequestLog(ClientRequestWrapper request) throws IOException {
        String traceID = ThreadContext.getTraceID();
        String method = request.getMethod().name().toUpperCase();
        String queryString = request.getURI().getRawQuery();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("method", method);
        log.put("requestUri", request.getURI().toString());
        log.put("requestHeaders", TraceUtils.getHeaders(request));
        log.put("requestPath", request.getURI().getPath());
        log.put("requestQuery", queryString);
        log.put("requestParams", URLSerializer.urlToMap(queryString, CharsetNames.UTF_8));
        if (method.equals("POST") || method.equals("PUT"))
            log.put("requestBody", request.getBodyInternal(null).getContent());
        return log;
    }

    public static void writeResponseLog(Map<String, Object> log, ClientResponseWrapper response, long tookMs) throws IOException {
        log.put("responseStatus", response.getRawStatusCode());
        log.put("responseHeaders", TraceUtils.getHeaders(response));
        log.put("responseBody", response.getBody().getContent());
        log.put("took", tookMs);
        logger.info(String.format("[%s] ==>Call Rest: %s", ThreadContext.getTraceID(), JsonSerializer.serialize(log)));
    }

    public static void writeError(Map<String, Object> log, Exception e, long tookMs) {
        String msg = ThrowableUtils.getMessage(e);
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error(String.format("[%s] ==>Call Rest Error: %s", ThreadContext.getTraceID(), JsonSerializer.serialize(log)));
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
