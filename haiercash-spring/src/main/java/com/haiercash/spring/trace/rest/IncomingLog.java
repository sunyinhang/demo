package com.haiercash.spring.trace.rest;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.servlet.DispatcherRequestWrapper;
import com.haiercash.spring.servlet.DispatcherResponseWrapper;
import com.haiercash.spring.trace.TraceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class IncomingLog {
    private static final Log logger = LogFactory.getLog(IncomingLog.class);

    public static void writeBeginLog(DispatcherRequestWrapper request) throws IOException {
        String method = request.getMethod().toUpperCase();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("method", method);
        log.put("servletPath", request.getServletPath());
        log.put("requestHeaders", TraceUtils.getHeaders(request));
        log.put("requestQuery", request.getQueryString());
        log.put("requestParams", TraceUtils.getParams(request));
        if (method.equals("POST") || method.equals("PUT"))
            log.put("requestBody", TraceUtils.getBody(request));
        logger.info("==>Servlet Begin: " + log);
    }

    public static void writeEndLog(DispatcherRequestWrapper request, DispatcherResponseWrapper response, long tookMs) throws IOException {
        String method = request.getMethod().toUpperCase();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("method", method);
        log.put("servletPath", request.getServletPath());
        log.put("responseStatus", response.getStatus());
        log.put("responseHeaders", TraceUtils.getHeaders(response));
        log.put("responseBody", TraceUtils.getBody(response));
        log.put("took", tookMs);
        logger.info("==>Servlet End: " + log);
    }

    public static void writeErrorLog(DispatcherRequestWrapper request, Exception e, long tookMs) {
        String method = request.getMethod().toUpperCase();
        String msg = ThrowableUtils.getString(e);
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("method", method);
        log.put("servletPath", request.getServletPath());
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error("==>Servlet Error: " + log);
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
