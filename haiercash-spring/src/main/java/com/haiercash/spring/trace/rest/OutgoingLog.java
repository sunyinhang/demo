package com.haiercash.spring.trace.rest;

import com.haiercash.core.collection.ArrayUtils;
import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.spring.client.ClientRequestWrapper;
import com.haiercash.spring.client.ClientResponseWrapper;
import com.haiercash.spring.context.ThreadContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-14.
 */
public final class OutgoingLog {
    private static final String INVOKE_BEGIN = "----------------调用服务----------------";
    private static final String INVOKE_SPLIT = "<<-------->>";
    private static final String INVOKE___END = "----------------------------------------";
    private static final Log logger = LogFactory.getLog(OutgoingLog.class);

    public static StringBuilder writeRequestLog(ClientRequestWrapper request) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.NewLine).append(INVOKE_BEGIN).append(Environment.NewLine);
        String method = request.getMethod().name().toUpperCase();//转大写
        builder.append("[").append(ThreadContext.getTraceID()).append("] ").append(method).append(" ").append(request.getURI().toString()).append(Environment.NewLine);
        //
        builder.append("Request Headers:").append(Environment.NewLine);
        writeHeaders(builder, request.getHeaders());
        //
        builder.append("Request Path:").append(Environment.NewLine);
        builder.append("    ").append(request.getURI().getPath()).append(Environment.NewLine);
        //
        builder.append("Request Query:").append(Environment.NewLine);
        String queryString = request.getURI().getRawQuery();
        if (StringUtils.isNotEmpty(queryString))
            builder.append("    ").append(queryString).append(Environment.NewLine);
        //
        builder.append("Request Params:").append(Environment.NewLine);
        if (StringUtils.isNotEmpty(queryString))
            writeParams(builder, URLSerializer.urlToMap(queryString, CharsetNames.UTF_8));
        //
        if (method.equals("POST") || method.equals("PUT")) {
            builder.append("Request Body:").append(Environment.NewLine);
            String content = request.getBodyInternal(null).getContent();
            if (StringUtils.isNotEmpty(content))
                builder.append("    ").append(content).append(Environment.NewLine);
        }
        builder.append(INVOKE_SPLIT).append(Environment.NewLine);
        return builder;
    }

    public static void writeResponseLog(StringBuilder builder, ClientResponseWrapper response, long tookMs) throws IOException {
        builder.append("Response Status:").append(Environment.NewLine);
        builder.append("    ").append(response.getRawStatusCode()).append(Environment.NewLine);
        //
        builder.append("Response Headers:").append(Environment.NewLine);
        writeHeaders(builder, response.getHeaders());
        //
        builder.append("Response Body:").append(Environment.NewLine);
        String content = response.getBody().getContent();
        if (StringUtils.isNotEmpty(content))
            builder.append("    ").append(content).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(INVOKE___END);
        logger.info(builder.toString());
    }

    public static void writeError(StringBuilder builder, Exception e, long tookMs) {
        builder.append("Error:").append(Environment.NewLine);
        builder.append(ThrowableUtils.getString(e)).append(Environment.NewLine);
        //
        builder.append("Took: ").append(tookMs).append(" ms").append(Environment.NewLine);
        builder.append(INVOKE___END);
        logger.info(builder.toString());
    }

    private static void writeHeaders(StringBuilder builder, HttpHeaders headers) {
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            builder.append("    ").append(header.getKey()).append(":");
            Iterator<String> headerValues = header.getValue().iterator();
            if (headerValues.hasNext()) {
                builder.append(headerValues.next());
                while (headerValues.hasNext())
                    builder.append("; ").append(headerValues.next());
            }
            builder.append(Environment.NewLine);
        }
    }

    private static void writeParams(StringBuilder builder, Map<String, String> paramMap) {
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
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
