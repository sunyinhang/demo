package com.haiercash.spring.client;

import com.haiercash.core.collection.EnumerationUtils;
import com.haiercash.spring.context.RequestContext;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.trace.TraceID;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 支持传递 Headers 的 RestTemplate.
 * Created by 许崇雷 on 2017-08-15.
 */
public class RestTemplateEx extends RestTemplate {
    final RestTemplateSupportedType supportedType;          //支持的类型
    boolean routeHeadersEnabled;                            //是否启用传递 Headers
    CaseInsensitiveKeyMap<Object> ignoreHeaders;            //忽略的 Headers

    public RestTemplateEx(RestTemplateSupportedType supportedType) {
        super(Collections.singletonList(null));
        if (supportedType == null)
            throw new NullPointerException("supportedType can not be null");
        this.supportedType = supportedType;
        //must set converters after construct
    }

    //发起请求
    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        return super.doExecute(url, method, new RequestCallbackWrapper(requestCallback), responseExtractor);
    }

    //创建请求
    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
        ClientHttpRequest clientHttpRequest = super.createRequest(url, method);
        return clientHttpRequest instanceof AbstractClientHttpRequest
                ? new ClientRequestWrapper((AbstractClientHttpRequest) clientHttpRequest)
                : clientHttpRequest;
    }

    //请求回调包装器
    private class RequestCallbackWrapper implements RequestCallback {
        private static final String NAME_TOKEN = "token";
        private static final String NAME_CHANNEL = "channel";
        private static final String NAME_CHANNEL_NO = "channel_no";
        private static final String NAME_CHANNEL_NO_DEL = "channelNo";
        private final RequestCallback requestCallback;

        //构造函数
        private RequestCallbackWrapper(RequestCallback requestCallback) {
            this.requestCallback = requestCallback;
        }

        //放入 Header
        private void putContextHeaders(ClientHttpRequest ribbonRequest) throws IOException {
            //通用 Header
            if (ThreadContext.exists()) {
                ribbonRequest.getHeaders().set(TraceID.NAME, ThreadContext.getTraceID());
                if (!ribbonRequest.getHeaders().containsKey(NAME_TOKEN))
                    ribbonRequest.getHeaders().set(NAME_TOKEN, ThreadContext.getToken());
                if (!ribbonRequest.getHeaders().containsKey(NAME_CHANNEL))
                    ribbonRequest.getHeaders().set(NAME_CHANNEL, ThreadContext.getChannel());
                if (!ribbonRequest.getHeaders().containsKey(NAME_CHANNEL_NO))
                    ribbonRequest.getHeaders().set(NAME_CHANNEL_NO, ThreadContext.getChannelNo());
            }
            //未启用传递 Header 功能
            if (!RestTemplateEx.this.routeHeadersEnabled)
                return;
            //原始 Header
            if (RequestContext.exists()) {
                HttpServletRequest sourceRequest = RequestContext.getRequest();
                Enumeration<String> headerNames = sourceRequest.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    //已有或敏感 Header 不覆盖
                    if (ribbonRequest.getHeaders().containsKey(headerName) || RestTemplateEx.this.ignoreHeaders.containsKey(headerName))
                        continue;
                    //设置 headerName headerValues
                    List<String> headerValues = EnumerationUtils.toList(sourceRequest.getHeaders(headerName));
                    ribbonRequest.getHeaders().put(headerName, headerValues);
                }
            }
            //移除无效 Header
            ribbonRequest.getHeaders().remove(NAME_CHANNEL_NO_DEL);
        }

        //执行回调
        @Override
        public void doWithRequest(ClientHttpRequest ribbonRequest) throws IOException {
            if (this.requestCallback != null)
                this.requestCallback.doWithRequest(ribbonRequest);
            putContextHeaders(ribbonRequest);
        }
    }
}
