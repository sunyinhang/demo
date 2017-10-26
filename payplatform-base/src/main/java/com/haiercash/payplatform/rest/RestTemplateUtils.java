package com.haiercash.payplatform.rest;

import com.bestvike.collection.MapUtils;
import com.bestvike.lang.Convert;
import com.haiercash.payplatform.client.RestTemplateProvider;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public final class RestTemplateUtils {
    private static final String ERROR_SERVER_MSG = "外部系统发生错误:HTTP-%s";

    private RestTemplateUtils() {
    }

    private static RestTemplate getRestTemplate() {
        return RestTemplateProvider.getRestTemplateNormal();
    }

    public static String getForString(String url) {
        return getForString(url, null, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables) {
        return getForString(url, uriVariables, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public static String deleteForString(String url) {
        return deleteForString(url, null, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables) {
        return deleteForString(url, uriVariables, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public static String postForString(String url, Object request) {
        return postForString(url, request, null);
    }

    public static String postForString(String url, Object request, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public static String putForString(String url, Object request) {
        return putForString(url, request, null);
    }

    public static String putForString(String url, Object request, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(request, headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }
}
