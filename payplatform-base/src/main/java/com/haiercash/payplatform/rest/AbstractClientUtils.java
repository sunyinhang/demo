package com.haiercash.payplatform.rest;

import com.bestvike.collection.MapUtils;
import com.bestvike.lang.Convert;
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
public abstract class AbstractClientUtils {
    private static final String ERROR_SERVER_MSG = "外部系统发生错误:HTTP-%s";

    protected abstract RestTemplate getRestTemplate();

    public String getForStringCore(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public String deleteForStringCore(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public String postForStringCore(String url, Object request, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    public String putForStringCore(String url, Object request, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(request, headers), String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK)
            return responseEntity.getBody();
        throw new RuntimeException(String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }
}
