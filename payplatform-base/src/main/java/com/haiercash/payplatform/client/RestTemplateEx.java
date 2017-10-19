package com.haiercash.payplatform.client;

import com.bestvike.collection.EnumerationUtils;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.config.HttpMessageConvertersAutoConfiguration;
import com.haiercash.payplatform.context.RequestContext;
import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.converter.FastJsonHttpMessageConverterEx;
import com.haiercash.payplatform.diagnostics.TraceID;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringListProperty;
import com.netflix.config.DynamicStringProperty;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 支持传递 Headers 的 RestTemplate.
 * Created by 许崇雷 on 2017-08-15.
 */
public class RestTemplateEx extends RestTemplate {
    private boolean inited; //实例是否初始化

    public RestTemplateEx() {
        super(Collections.singletonList(null));
    }

    //初始化.必须延迟执行,否则无法读取配置
    private void init() {
        if (this.inited)
            return;
        synchronized (this) {
            if (this.inited)
                return;
            RestTemplateConfig.init();
            this.setMessageConverters(RestTemplateConfig.MESSAGE_CONVERTERS);
            this.inited = true;
        }
    }

    @Override
    public List<HttpMessageConverter<?>> getMessageConverters() {
        this.init();
        return super.getMessageConverters();
    }

    //发起请求
    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        this.init();
        return super.doExecute(url, method, new RequestCallbackWrapper(requestCallback), responseExtractor);
    }

    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
        ClientHttpRequest clientHttpRequest = super.createRequest(url, method);
        return clientHttpRequest instanceof AbstractClientHttpRequest
                ? new ClientRequestWrapper((AbstractClientHttpRequest) clientHttpRequest)
                : clientHttpRequest;
    }

    //请求回调包装器
    private static class RequestCallbackWrapper implements RequestCallback {
        private final RequestCallback requestCallback;

        //构造函数
        private RequestCallbackWrapper(RequestCallback requestCallback) {
            this.requestCallback = requestCallback;
        }

        //执行回调
        @Override
        public void doWithRequest(ClientHttpRequest ribbonRequest) throws IOException {
            if (this.requestCallback != null)
                this.requestCallback.doWithRequest(ribbonRequest);
            putContextHeaders(ribbonRequest);
        }

        //放入 Header
        private static void putContextHeaders(ClientHttpRequest ribbonRequest) throws IOException {
            //通用 Header
            if (ThreadContext.exists()) {
                ribbonRequest.getHeaders().set(TraceID.NAME, ThreadContext.getTraceID());
                ribbonRequest.getHeaders().set("token", ThreadContext.getTraceID());
                ribbonRequest.getHeaders().set("channel", ThreadContext.getTraceID());
                ribbonRequest.getHeaders().set("channel_no", ThreadContext.getTraceID());
            }
            //未启用传递 Header 功能
            if (!RestTemplateConfig.ROUTE_HEADERS_ENABLED)
                return;
            //原始 Header
            if (RequestContext.exists()) {
                HttpServletRequest sourceRequest = RequestContext.getRequest();
                Enumeration<String> headerNames = sourceRequest.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    //已有或敏感 Header 不覆盖
                    if (ribbonRequest.getHeaders().containsKey(headerName) || RestTemplateConfig.IGNORE_HEADERS.containsKey(headerName))
                        continue;
                    //设置 headerName headerValues
                    List<String> headerValues = EnumerationUtils.toList(sourceRequest.getHeaders(headerName));
                    ribbonRequest.getHeaders().put(headerName, headerValues);
                }
            }
            //移除无效 Header
            ribbonRequest.getHeaders().remove("channelno");
        }
    }

    //配置信息
    private static class RestTemplateConfig {
        private static final Object PRESENT = new Object();
        private static final List<String> SYSTEM_IGNORE_HEADERS = Arrays.asList("Accept", "Accept-Encoding", "Accept-Language", "Content-Type", "Content-Length", "Cookie", "Set-Cookie", "Authorization", "Connection", "Host", "User-Agent");
        private static final DynamicBooleanProperty ROUTE_HEADERS_ENABLED_PROPERTY = DynamicPropertyFactory.getInstance().getBooleanProperty("ribbon.route-headers-enabled", false);
        private static final DynamicStringListProperty USER_IGNORE_HEADERS_PROPERTY = new DynamicStringListProperty("ribbon.ignore-headers", (String) null);
        private static final DynamicStringProperty PREFERRED_JSON_MAPPER = DynamicPropertyFactory.getInstance().getStringProperty(HttpMessageConvertersAutoConfiguration.PREFERRED_MAPPER_PROPERTY, StringUtils.EMPTY);

        private static boolean INITED;                                      //是否已初始化
        private static boolean ROUTE_HEADERS_ENABLED;                       //是否启用传递 Headers
        private static CaseInsensitiveKeyMap<Object> IGNORE_HEADERS;        //忽略的 Headers
        private static List<HttpMessageConverter<?>> MESSAGE_CONVERTERS;    //格式转换器

        //初始化
        private static void init() {
            if (INITED)
                return;
            initIgnoreHeaders();
            initMessageConverters();
            INITED = true;
        }

        //初始化忽略的 Headers
        private static void initIgnoreHeaders() {
            ROUTE_HEADERS_ENABLED = ROUTE_HEADERS_ENABLED_PROPERTY.get();
            CaseInsensitiveKeyMap<Object> set = new CaseInsensitiveKeyMap<>();
            for (String headerName : SYSTEM_IGNORE_HEADERS)
                set.put(headerName, PRESENT);
            List<String> userIgnoreHeaders = USER_IGNORE_HEADERS_PROPERTY.get();
            if (!org.apache.commons.collections.CollectionUtils.isEmpty(userIgnoreHeaders))
                for (String headerName : userIgnoreHeaders)
                    set.put(headerName, PRESENT);
            IGNORE_HEADERS = set;
        }

        //创建数据转换器
        private static void initMessageConverters() {
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            messageConverters.add(new ByteArrayHttpMessageConverter());
            messageConverters.add(new StringHttpMessageConverter());
            messageConverters.add(new ResourceHttpMessageConverter());
            messageConverters.add(new SourceHttpMessageConverter());
            messageConverters.add(new AllEncompassingFormHttpMessageConverter());
            String preferredJsonMapper = PREFERRED_JSON_MAPPER.get().toLowerCase();
            switch (preferredJsonMapper) {
                case HttpMessageConvertersAutoConfiguration.PREFERRED_MAPPER_PROPERTY_JACKSON:
                    messageConverters.add(new MappingJackson2HttpMessageConverter());
                    break;
                case HttpMessageConvertersAutoConfiguration.PREFERRED_MAPPER_PROPERTY_GSON:
                    messageConverters.add(new GsonHttpMessageConverter());
                    break;
                case HttpMessageConvertersAutoConfiguration.PREFERRED_MAPPER_PROPERTY_FASTJSON:
                    messageConverters.add(new FastJsonHttpMessageConverterEx());
                    break;
                default:
                    messageConverters.add(new MappingJackson2HttpMessageConverter());
                    break;
            }
            MESSAGE_CONVERTERS = messageConverters;
        }
    }
}
