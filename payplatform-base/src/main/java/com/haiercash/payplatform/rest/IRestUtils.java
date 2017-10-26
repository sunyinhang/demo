package com.haiercash.payplatform.rest;

import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IRestUtils<TResponse extends IResponse> {
    TResponse getForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers);

    IResponse deleteForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers);

    IResponse postForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers);

    IResponse putForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers);
}
