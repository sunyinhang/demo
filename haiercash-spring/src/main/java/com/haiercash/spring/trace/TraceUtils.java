package com.haiercash.spring.trace;

import com.haiercash.core.collection.EnumerationUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.spring.client.ClientRequestWrapper;
import com.haiercash.spring.client.ClientResponseWrapper;
import com.haiercash.spring.servlet.DispatcherRequestWrapper;
import com.haiercash.spring.servlet.DispatcherResponseWrapper;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.Message;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    //region rabbit

    public static String getMsgId(Message message) {
        return message.getHeaders().getId().toString();
    }

    public static String getMsgId(org.springframework.amqp.core.Message message) {
        return message.getMessageProperties().getMessageId();
    }

    public static Map<String, Object> getHeaders(Message message) {
        return message.getHeaders();
    }

    public static Map<String, String> getHeaders(org.springframework.amqp.core.Message message) {
        MessageProperties properties = message.getMessageProperties();
        Map<String, String> headers = new LinkedHashMap<>(2);
        headers.put("content-type", properties.getContentType());
        headers.put("content-encoding", properties.getContentEncoding());
        return headers;
    }

    public static String getBody(Message message) {
        return getBodyCore((byte[]) message.getPayload(), Convert.toString(message.getHeaders().get(AmqpHeaders.CONTENT_ENCODING)));
    }

    public static String getBody(org.springframework.amqp.core.Message message) {
        return getBodyCore(message.getBody(), message.getMessageProperties().getContentEncoding());
    }

    private static String getBodyCore(byte[] body, String encoding) {
        encoding = Convert.defaultString(encoding, TraceConfig.DEFAULT_CHARSET_NAME);
        try {
            return new String(body, encoding);
        } catch (Exception e) {
            return TraceConfig.BODY_PARSE_FAIL;
        }
    }

    //endregion

    //region rest

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            headers.put(headerName, StringUtils.join(TraceConfig.HEADER_SEPARATOR, EnumerationUtils.toList(headerValues)));
        }
        return headers;
    }

    public static Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String headerName : response.getHeaderNames())
            headers.put(headerName, StringUtils.join(TraceConfig.HEADER_SEPARATOR, response.getHeaders(headerName)));
        return headers;
    }

    public static Map<String, String> getHeaders(HttpMessage message) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : message.getHeaders().entrySet())
            headers.put(entry.getKey(), StringUtils.join(TraceConfig.HEADER_SEPARATOR, entry.getValue()));
        return headers;
    }

    public static Map<String, String> getParams(ServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet())
            params.put(entry.getKey(), StringUtils.join(TraceConfig.HEADER_SEPARATOR, entry.getValue()));
        return params;
    }

    public static Map<String, String> getParams(HttpRequest request) {
        return URLSerializer.urlToMap(request.getURI().getRawQuery());
    }

    public static String getBody(DispatcherRequestWrapper request) throws IOException {
        return request.getInputStream().getContent();
    }

    public static String getBody(DispatcherResponseWrapper response) throws IOException {
        return response.isUsingWriter() ? TraceConfig.BODY_RESOURCE : response.getOutputStream().getContent();
    }

    public static String getBody(ClientRequestWrapper request) throws IOException {
        return request.getBodyInternal(null).getContent();
    }

    public static String getBody(ClientResponseWrapper response) throws IOException {
        return response.getBody().getContent();
    }

    //endregion
}
