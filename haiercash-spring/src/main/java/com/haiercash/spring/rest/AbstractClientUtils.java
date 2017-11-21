package com.haiercash.spring.rest;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.reflect.GenericTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public abstract class AbstractClientUtils<TResponse> {
    private static final String ERROR_NULL_MSG = "外部服务未返回任何数据";
    private static final String ERROR_SERVER_MSG = "外部系统发生错误:HTTP-%s";

    protected abstract RestTemplate getRestTemplate();

    protected final GenericTypeReference<TResponse> createResponseTypeReference(Type bodyType) {
        return new GenericTypeReference<>(bodyType);
    }

    public TResponse getForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), responseTypeReference);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            if (responseEntity.getBody() == null)
                throw new RuntimeException(ERROR_NULL_MSG);
            return responseEntity.getBody();
        }
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public TResponse deleteForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), responseTypeReference);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            if (responseEntity.getBody() == null)
                throw new RuntimeException(ERROR_NULL_MSG);
            return responseEntity.getBody();
        }
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public TResponse postForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), responseTypeReference);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            if (responseEntity.getBody() == null)
                throw new RuntimeException(ERROR_NULL_MSG);
            return responseEntity.getBody();
        }
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public TResponse putForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(request, headers), responseTypeReference);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            if (responseEntity.getBody() == null)
                throw new RuntimeException(ERROR_NULL_MSG);
            return responseEntity.getBody();
        }
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }
}
