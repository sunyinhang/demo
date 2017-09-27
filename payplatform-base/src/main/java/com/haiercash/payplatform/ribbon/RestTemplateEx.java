package com.haiercash.payplatform.ribbon;

import com.bestvike.collection.CollectionUtils;
import com.bestvike.collection.EnumerationUtils;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.config.HttpMessageConvertersAutoConfiguration;
import com.haiercash.payplatform.converter.FastJsonHttpMessageConverterEx;
import com.haiercash.payplatform.filter.RequestContext;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringListProperty;
import com.netflix.config.DynamicStringProperty;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.http.HttpMethod;
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
import java.util.Enumeration;
import java.util.List;

/**
 * 支持传递 Headers 的 RestTemplate.
 *
 * @author 许崇雷
 * @date 2017/8/15
 */
public class RestTemplateEx extends RestTemplate {
    private static final Object PRESENT = new Object();
    private static final List<String> SYSTEM_IGNORE_HEADERS = Arrays.asList("Accept", "Accept-Encoding", "Accept-Language", "Content-Type", "Content-Length", "Cookie", "Set-Cookie", "Authorization", "Connection", "Host", "User-Agent");
    private static final DynamicBooleanProperty ROUTE_HEADERS_ENABLED_PROPERTY = DynamicPropertyFactory.getInstance().getBooleanProperty("ribbon.route-headers-enabled", false);
    private static final DynamicStringListProperty USER_IGNORE_HEADERS_PROPERTY = new DynamicStringListProperty("ribbon.ignore-headers", (String) null);
    private static final DynamicStringProperty PREFERRED_JSON_MAPPER = DynamicPropertyFactory.getInstance().getStringProperty(HttpMessageConvertersAutoConfiguration.PREFERRED_MAPPER_PROPERTY, StringUtils.EMPTY);


    private static boolean routeHeadersEnabled;                 //是否启用传递 Headers
    private static CaseInsensitiveKeyMap<Object> ignoreHeaders; //忽略的 Headers

    //构造函数
    public RestTemplateEx() {
        super(createMessageConverters());
    }

    //创建数据转换器
    private static List<HttpMessageConverter<?>> createMessageConverters() {
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
        return messageConverters;
    }

    //初始化忽略的 Headers
    private static void initIgnoreHeaders() {
        if (ignoreHeaders != null)
            return;
        routeHeadersEnabled = ROUTE_HEADERS_ENABLED_PROPERTY.get();
        CaseInsensitiveKeyMap<Object> set = new CaseInsensitiveKeyMap<>();
        for (String headerName : SYSTEM_IGNORE_HEADERS)
            set.put(headerName, PRESENT);
        List<String> userIgnoreHeaders = USER_IGNORE_HEADERS_PROPERTY.get();
        if (!CollectionUtils.isEmpty(userIgnoreHeaders))
            for (String headerName : userIgnoreHeaders)
                set.put(headerName, PRESENT);
        ignoreHeaders = set;
    }

    //发起请求
    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        return super.doExecute(url, method, new RequestCallbackWrapper(requestCallback), responseExtractor);
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

        //如果启用上下文 Headers 传递且存在上下文.则将 Headers 放入 ribbonRequest.已有的 Headers 不会被覆盖,敏感 Headers 不会被放入
        private static void putContextHeaders(ClientHttpRequest ribbonRequest) throws IOException {
            initIgnoreHeaders();
            if (!routeHeadersEnabled || !RequestContext.exists())
                return;
            HttpServletRequest sourceRequest = RequestContext.get().getRequest();

            //添加请求 Header
            Enumeration<String> headerNames = sourceRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                //不覆盖
                if (ribbonRequest.getHeaders().containsKey(headerName) || ignoreHeaders.containsKey(headerName))
                    continue;
                //设置 headerName headerValues
                List<String> headerValues = EnumerationUtils.toList(sourceRequest.getHeaders(headerName));
                ribbonRequest.getHeaders().put(headerName, headerValues);
            }
        }
    }
}
