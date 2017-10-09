package com.haiercash.payplatform.rest;

import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IRestUtil<TResponse extends IRestResponse> {
    TResponse getCore(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers);

    TResponse deleteCore(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers);

    TResponse postCore(String url, Object request, MultiValueMap<String, String> headers);

    TResponse putCore(String url, Object request, MultiValueMap<String, String> headers);
}
