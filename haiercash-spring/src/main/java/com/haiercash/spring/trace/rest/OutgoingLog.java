package com.haiercash.spring.trace.rest;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.client.ClientRequestWrapper;
import com.haiercash.spring.client.ClientResponseWrapper;
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
        String method = request.getMethod().name().toUpperCase();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("method", method);
        log.put("requestUri", request.getURI().toString());
        log.put("requestHeaders", TraceUtils.getHeaders(request));
        log.put("requestPath", request.getURI().getPath());
        log.put("requestQuery", request.getURI().getRawQuery());
        log.put("requestParams", TraceUtils.getParams(request));
        if (method.equals("POST") || method.equals("PUT"))
            log.put("requestBody", TraceUtils.getBody(request));
        return log;
    }

    public static void writeResponseLog(Map<String, Object> log, ClientResponseWrapper response, long tookMs) throws IOException {
        log.put("responseStatus", response.getRawStatusCode());
        log.put("responseHeaders", TraceUtils.getHeaders(response));
        log.put("responseBody", TraceUtils.getBody(response));
        log.put("took", tookMs);
        logger.info("==>Call Rest: " + log);
    }

    public static void writeError(Map<String, Object> log, Exception e, long tookMs) {
        log.put("error", ThrowableUtils.getMessage(e));
        log.put("took", tookMs);
        logger.error("==>Call Rest Error: " + log);
    }
}
