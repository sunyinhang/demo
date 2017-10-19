package com.haiercash.payplatform.diagnostics;

import com.bestvike.collection.ArrayUtils;
import com.bestvike.lang.Environment;
import com.bestvike.lang.StringUtils;
import com.bestvike.lang.ThrowableUtils;
import com.haiercash.payplatform.filter.DispatcherRequestWrapper;
import com.haiercash.payplatform.filter.DispatcherResponseWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class IncomingLog {
    private static final String REQ_BEGIN = "----------------收到请求-------->>>>>>>>";
    private static final String REQ___END = "-------------------------------->>>>>>>>";
    private static final String RES_BEGIN = "<<<<<<<<--------返回响应----------------";
    private static final String RES___END = "<<<<<<<<--------------------------------";
    private static final String ERR_BEGIN = "<<<<<<<<--------异常响应----------------";
    private static final String ERR___END = "<<<<<<<<--------------------------------";
    private static final Log logger = LogFactory.getLog(IncomingLog.class);

    public static void writeRequestLog(DispatcherRequestWrapper request) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(REQ_BEGIN).append(Environment.NewLine);
        String method = request.getMethod().toUpperCase();//转大写
        builder.append("[").append(TraceID.current()).append("] ").append(method).append(" ").append(request.getServletPath()).append(Environment.NewLine);
        //
        builder.append("Request Headers:").append(Environment.NewLine);
        writeHeaders(builder, request);
        //
        builder.append("Request Query:").append(Environment.NewLine);
        String queryString = request.getQueryString();
        if (StringUtils.isNotEmpty(queryString))
            builder.append("    ").append(queryString).append(Environment.NewLine);
        //
        builder.append("Request Params:").append(Environment.NewLine);
        writeParams(builder, request);
        //
        if (method.equals("POST") || method.equals("PUT")) {
            builder.append("Request Body:").append(Environment.NewLine);
            String content = request.getInputStream().getContent();
            if (StringUtils.isNotEmpty(content))
                builder.append("    ").append(content).append(Environment.NewLine);
        }
        builder.append(REQ___END);
        logger.info(builder.toString());
    }

    public static void writeResponseLog(DispatcherRequestWrapper request, DispatcherResponseWrapper response, long tookMs) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(RES_BEGIN).append(Environment.NewLine);
        String method = request.getMethod().toUpperCase();//转大写
        builder.append("[").append(TraceID.current()).append("] ").append(method).append(" ").append(request.getServletPath()).append(Environment.NewLine);
        //
        builder.append("Response Headers:").append(Environment.NewLine);
        writeHeaders(builder, response);
        //
        builder.append("Response Body:").append(Environment.NewLine);
        String content = response.getOutputStream().getContent();
        if (StringUtils.isNotEmpty(content))
            builder.append("    ").append(content).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(RES___END);
        logger.info(builder.toString());
    }

    public static void writeError(DispatcherRequestWrapper request, Exception e, long tookMs) {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(ERR_BEGIN).append(Environment.NewLine);
        String method = request.getMethod().toUpperCase();//转大写
        builder.append("[").append(TraceID.current()).append("] ").append(method).append(" ").append(request.getServletPath()).append(Environment.NewLine);
        //
        builder.append("Error:").append(Environment.NewLine);
        builder.append(ThrowableUtils.getString(e)).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(ERR___END);
        logger.info(builder.toString());
    }

    private static void writeHeaders(StringBuilder builder, HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            builder.append("    ").append(headerName).append(":");
            Enumeration<String> headerValues = request.getHeaders(headerName);
            if (headerValues.hasMoreElements()) {
                builder.append(headerValues.nextElement());
                while (headerValues.hasMoreElements())
                    builder.append("; ").append(headerValues.nextElement());
            }
            builder.append(Environment.NewLine);
        }
    }

    private static void writeHeaders(StringBuilder builder, HttpServletResponse request) {
        for (String headerName : request.getHeaderNames()) {
            builder.append("    ").append(headerName).append(":");
            Iterator<String> headerValues = request.getHeaders(headerName).iterator();
            if (headerValues.hasNext()) {
                builder.append(headerValues.next());
                while (headerValues.hasNext())
                    builder.append("; ").append(headerValues.next());
            }
            builder.append(Environment.NewLine);
        }
    }

    private static void writeParams(StringBuilder builder, HttpServletRequest request) {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            builder.append("    ").append(entry.getKey()).append(":");
            Iterator<String> entryValues = ArrayUtils.asIterator(entry.getValue());
            if (entryValues.hasNext()) {
                builder.append(entryValues.next());
                while (entryValues.hasNext())
                    builder.append("; ").append(entryValues.next());
            }
            builder.append(Environment.NewLine);
        }
    }
}
