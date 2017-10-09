package com.haiercash.payplatform.rest.cmisacq;

import com.haiercash.payplatform.rest.AbstractRestUtil;
import com.haiercash.payplatform.rest.RestTemplateProvider;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public class CmisAcqUtil<TBody> extends AbstractRestUtil<CmisAcqResponse<TBody>> {
    @Override
    protected RestTemplate getRestTemplate() {
        return RestTemplateProvider.getRestTemplate();
    }

    @Override
    protected CmisAcqResponse<TBody> createResponse(String retFlag, String retMsg) {
        return CmisAcqResponse.create(retFlag, retMsg);
    }

    public static <TBody> CmisAcqResponse<TBody> get(String url) {
        return new CmisAcqUtil<TBody>().getCore(url, null, null);
    }

    public static <TBody> CmisAcqResponse<TBody> get(String url, Map<String, ?> uriVariables) {
        return new CmisAcqUtil<TBody>().getCore(url, uriVariables, null);
    }

    public static <TBody> CmisAcqResponse<TBody> get(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return new CmisAcqUtil<TBody>().getCore(url, uriVariables, headers);
    }

    public static <TBody> CmisAcqResponse<TBody> delete(String url) {
        return new CmisAcqUtil<TBody>().deleteCore(url, null, null);
    }

    public static <TBody> CmisAcqResponse<TBody> delete(String url, Map<String, ?> uriVariables) {
        return new CmisAcqUtil<TBody>().deleteCore(url, uriVariables, null);
    }

    public static <TBody> CmisAcqResponse<TBody> delete(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return new CmisAcqUtil<TBody>().deleteCore(url, uriVariables, headers);
    }

    public static <TBody> CmisAcqResponse<TBody> post(String url, Object request) {
        return new CmisAcqUtil<TBody>().postCore(url, request, null);
    }

    public static <TBody> CmisAcqResponse<TBody> post(String url, Object request, MultiValueMap<String, String> headers) {
        return new CmisAcqUtil<TBody>().postCore(url, request, headers);
    }

    public static <TBody> CmisAcqResponse<TBody> put(String url, Object request) {
        return new CmisAcqUtil<TBody>().putCore(url, request, null);
    }

    public static <TBody> CmisAcqResponse<TBody> put(String url, Object request, MultiValueMap<String, String> headers) {
        return new CmisAcqUtil<TBody>().putCore(url, request, headers);
    }
}
