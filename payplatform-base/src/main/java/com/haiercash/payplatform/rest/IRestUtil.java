package com.haiercash.payplatform.rest;

import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public interface IRestUtil<T extends IRestResponse> {
    T getCore(String api, Map<String, ?> uriVariables, MultiValueMap<String, String> headers);

    T deleteCore(String api, Map<String, ?> uriVariables, MultiValueMap<String, String> headers);

    T postCore(String api, Object request, MultiValueMap<String, String> headers);

    T putCore(String api, Object request, MultiValueMap<String, String> headers);
}
