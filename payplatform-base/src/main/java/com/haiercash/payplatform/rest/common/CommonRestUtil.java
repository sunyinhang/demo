package com.haiercash.payplatform.rest.common;

import com.haiercash.payplatform.rest.AbstractRestUtil;
import com.haiercash.payplatform.rest.RestTemplateProvider;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public class CommonRestUtil<TBody> extends AbstractRestUtil<CommonResponse<TBody>> {
    @Override
    protected RestTemplate getRestTemplate() {
        return RestTemplateProvider.getRestTemplate();
    }

    @Override
    protected CommonResponse<TBody> createResponse(String retFlag, String retMsg) {
        return CommonResponse.create(retFlag, retMsg);
    }

    public static <TBody> CommonResponse<TBody> get(String url) {
        return new CommonRestUtil<TBody>().getCore(url, null, null);
    }

    public static <TBody> CommonResponse<TBody> get(String url, Map<String, ?> uriVariables) {
        return new CommonRestUtil<TBody>().getCore(url, uriVariables, null);
    }

    public static <TBody> CommonResponse<TBody> get(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return new CommonRestUtil<TBody>().getCore(url, uriVariables, headers);
    }

    public static <TBody> CommonResponse<TBody> delete(String url) {
        return new CommonRestUtil<TBody>().deleteCore(url, null, null);
    }

    public static <TBody> CommonResponse<TBody> delete(String url, Map<String, ?> uriVariables) {
        return new CommonRestUtil<TBody>().deleteCore(url, uriVariables, null);
    }

    public static <TBody> CommonResponse<TBody> delete(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return new CommonRestUtil<TBody>().deleteCore(url, uriVariables, headers);
    }

    public static <TBody> CommonResponse<TBody> post(String url, Object request) {
        return new CommonRestUtil<TBody>().postCore(url, request, null);
    }

    public static <TBody> CommonResponse<TBody> post(String url, Object request, MultiValueMap<String, String> headers) {
        return new CommonRestUtil<TBody>().postCore(url, request, headers);
    }

    public static <TBody> CommonResponse<TBody> put(String url, Object request) {
        return new CommonRestUtil<TBody>().putCore(url, request, null);
    }

    public static <TBody> CommonResponse<TBody> put(String url, Object request, MultiValueMap<String, String> headers) {
        return new CommonRestUtil<TBody>().putCore(url, request, headers);
    }
}
