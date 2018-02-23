package com.haiercash.spring.feign.core;

import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.rest.AbstractRestUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
@Data
@AllArgsConstructor
public final class FeignRequest {
    private final String url;
    private final HttpMethod method;
    private final Object body;
    private final Type responseType;
    private final Map<String, String> uriVariables = new LinkedHashMap<>();
    private final HttpHeaders headers = new HttpHeaders();

    public Object invoke(boolean loadBalanced) {
        MediaType mediaType = this.headers.getContentType();
        boolean isXml = mediaType != null && mediaType.getSubtype().contains("xml");
        RestTemplate restTemplate = loadBalanced
                ? (isXml ? RestTemplateProvider.getRibbonXmlRestTemplate() : RestTemplateProvider.getRibbonJsonRestTemplate())
                : (isXml ? RestTemplateProvider.getXmlRestTemplate() : RestTemplateProvider.getJsonRestTemplate());
        return AbstractRestUtils.exchange(restTemplate, this.getUrl(), this.getMethod(), this.getBody(), this.getResponseType(), this.getUriVariables(), this.getHeaders());
    }

    interface Factory {
        FeignRequest create(Object[] argv);
    }
}
