package com.haiercash.spring.trace.rest;

import com.haiercash.core.collection.EnumerationUtils;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import com.haiercash.spring.servlet.DispatcherRequestWrapper;
import com.haiercash.spring.servlet.DispatcherResponseWrapper;
import com.haiercash.spring.trace.TraceConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class IncomingLog {
    private static final Log logger = LogFactory.getLog(IncomingLog.class);

    public static void writeRequestLog(DispatcherRequestWrapper request) throws IOException {
        String traceID = ThreadContext.getTraceID();
        String method = request.getMethod().toUpperCase();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("method", method);
        log.put("servletPath", request.getServletPath());
        log.put("requestHeaders", getRequestHeaders(request));
        log.put("requestQuery", request.getQueryString());
        log.put("requestParams", request.getParameterMap());
        if (method.equals("POST") || method.equals("PUT"))
            log.put("requestBody", request.getInputStream().getContent());
        logger.info(String.format("[%s] ==>Servlet Begin: %s", traceID, JsonSerializer.serialize(log)));
    }

    public static void writeResponseLog(DispatcherRequestWrapper request, DispatcherResponseWrapper response, long tookMs) throws IOException {
        String traceID = ThreadContext.getTraceID();
        String method = request.getMethod().toUpperCase();
        String responseBody = response.isUsingWriter() ? TraceConfig.BODY_RESOURCE : response.getOutputStream().getContent();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("method", method);
        log.put("servletPath", request.getServletPath());
        log.put("responseStatus", response.getStatus());
        log.put("responseHeaders", getResponseHeaders(response));
        log.put("responseBody", responseBody);
        log.put("took", tookMs);
        logger.info(String.format("[%s] ==>Servlet End: %s", traceID, JsonSerializer.serialize(log)));
    }

    public static void writeErrorLog(DispatcherRequestWrapper request, Exception e, long tookMs) {
        String traceID = ThreadContext.getTraceID();
        String msg = ThrowableUtils.getString(e);
        String method = request.getMethod().toUpperCase();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("traceID", traceID);
        log.put("method", method);
        log.put("servletPath", request.getServletPath());
        log.put("error", msg);
        log.put("took", tookMs);
        logger.error(String.format("[%s] ==>Servlet Error: %s", traceID, JsonSerializer.serialize(log)));
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }

    private static Map<String, Collection<String>> getRequestHeaders(HttpServletRequest request) {
        Map<String, Collection<String>> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            headers.put(headerName, EnumerationUtils.toList(headerValues));
        }
        return headers;
    }

    private static Map<String, Collection<String>> getResponseHeaders(HttpServletResponse response) {
        Map<String, Collection<String>> headers = new LinkedHashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeaders(headerName));
        }
        return headers;
    }
}
