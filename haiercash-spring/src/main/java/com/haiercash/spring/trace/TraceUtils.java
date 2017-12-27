package com.haiercash.spring.trace;

import com.haiercash.core.collection.EnumerationUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.HttpMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-12-27.
 */
public final class TraceUtils {
    private TraceUtils() {
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            headers.put(headerName, StringUtils.join("; ", EnumerationUtils.toList(headerValues)));
        }
        return headers;
    }

    public static Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String headerName : response.getHeaderNames())
            headers.put(headerName, StringUtils.join("; ", response.getHeaders(headerName)));
        return headers;
    }

    public static Map<String, String> getHeaders(HttpMessage message) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : message.getHeaders().entrySet())
            headers.put(entry.getKey(), StringUtils.join("; ", entry.getValue()));
        return headers;
    }

    public static Map<String, String> getHeaders(MessageProperties properties) {
        Map<String, String> headers = new LinkedHashMap<>(2);
        headers.put("content-type", properties.getContentType());
        headers.put("content-encoding", properties.getContentEncoding());
        return headers;
    }

    public static String getBody(byte[] body, String encoding) {
        encoding = Convert.defaultString(encoding, TraceConfig.DEFAULT_CHARSET_NAME);
        try {
            return new String(body, encoding);
        } catch (Exception e) {
            return TraceConfig.BODY_PARSE_FAIL;
        }
    }
}
