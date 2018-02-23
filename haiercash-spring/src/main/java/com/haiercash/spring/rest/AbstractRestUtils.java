package com.haiercash.spring.rest;

import com.haiercash.spring.client.RestTemplateUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public abstract class AbstractRestUtils {
    public <TResponse> TResponse getForCore(String url, Type responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return RestTemplateUtils.exchange(this.getRestTemplate(), url, HttpMethod.GET, null, responseType, uriVariables, headers);
    }

    public <TResponse> TResponse deleteForCore(String url, Type responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return RestTemplateUtils.exchange(this.getRestTemplate(), url, HttpMethod.DELETE, null, responseType, uriVariables, headers);
    }

    public <TResponse> TResponse postForCore(String url, Object body, Type responseType, MultiValueMap<String, String> headers) {
        return RestTemplateUtils.exchange(this.getRestTemplate(), url, HttpMethod.POST, body, responseType, null, headers);
    }

    public <TResponse> TResponse putForCore(String url, Object body, Type responseType, MultiValueMap<String, String> headers) {
        return RestTemplateUtils.exchange(this.getRestTemplate(), url, HttpMethod.PUT, body, responseType, null, headers);
    }

    protected abstract RestTemplate getRestTemplate();
}
