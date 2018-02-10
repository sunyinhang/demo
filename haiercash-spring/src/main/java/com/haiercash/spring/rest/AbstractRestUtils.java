package com.haiercash.spring.rest;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.reflect.GenericTypeReference;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
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
public abstract class AbstractRestUtils {
    private static final String ERROR_SERVER = ConstUtil.ERROR_CODE;
    private static final String ERROR_SERVER_MSG = "外部服务发生错误:HTTP-%s";
    private static final String ERROR_NULL = ConstUtil.ERROR_CODE;
    private static final String ERROR_NULL_MSG = "外部服务未返回任何数据";

    public static <TResponse> TResponse exchange(RestTemplate restTemplate, String url, HttpMethod method, Object body, Type responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = new GenericTypeReference<>(responseType);
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), responseTypeReference);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            if (responseEntity.getBody() == null)
                throw new BusinessException(ERROR_NULL, ERROR_NULL_MSG);
            return responseEntity.getBody();
        }
        throw new BusinessException(ERROR_SERVER, ERROR_SERVER_MSG + responseEntity.getStatusCodeValue());
    }

    public <TResponse> TResponse getForCore(String url, Type responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return exchange(this.getRestTemplate(), url, HttpMethod.GET, null, responseType, uriVariables, headers);
    }

    public <TResponse> TResponse deleteForCore(String url, Type responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return exchange(this.getRestTemplate(), url, HttpMethod.DELETE, null, responseType, uriVariables, headers);
    }

    public <TResponse> TResponse postForCore(String url, Object body, Type responseType, MultiValueMap<String, String> headers) {
        return exchange(this.getRestTemplate(), url, HttpMethod.POST, body, responseType, null, headers);
    }

    public <TResponse> TResponse putForCore(String url, Object body, Type responseType, MultiValueMap<String, String> headers) {
        return exchange(this.getRestTemplate(), url, HttpMethod.PUT, body, responseType, null, headers);
    }

    protected abstract RestTemplate getRestTemplate();
}
