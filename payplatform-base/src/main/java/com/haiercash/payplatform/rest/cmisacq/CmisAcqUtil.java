package com.haiercash.payplatform.rest.cmisacq;

import com.bestvike.reflect.GenericType;
import com.haiercash.payplatform.client.RestTemplateProvider;
import com.haiercash.payplatform.rest.AbstractRestUtils;
import com.haiercash.payplatform.rest.IResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public final class CmisAcqUtil {
    private static final RestUtils REST_UTIL = new RestUtils();
    private static final ParameterizedTypeImpl MAP_STRING_OBJECT_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);

    private CmisAcqUtil() {
    }

    private static Type getMapType() {
        return MAP_STRING_OBJECT_TYPE;
    }

    private static <TBody> AbstractRestUtils<IResponse<TBody>> getRestUtil() {
        return REST_UTIL;
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.getForCore(url, bodyType, null, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.getForCore(url, bodyType, uriVariables, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.getForCore(url, bodyType, uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.deleteForCore(url, bodyType, null, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.deleteForCore(url, bodyType, uriVariables, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.deleteForCore(url, bodyType, uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, Class<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.postForCore(url, request, bodyType, null);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.postForCore(url, request, bodyType, headers);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, Class<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.putForCore(url, request, bodyType, null);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.putForCore(url, request, bodyType, headers);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.getForCore(url, bodyType, null, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.getForCore(url, bodyType, uriVariables, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.getForCore(url, bodyType, uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.deleteForCore(url, bodyType, null, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.deleteForCore(url, bodyType, uriVariables, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.deleteForCore(url, bodyType, uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, GenericType<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.postForCore(url, request, bodyType, null);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.postForCore(url, request, bodyType, headers);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, GenericType<TBody> bodyType) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.putForCore(url, request, bodyType, null);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<TBody>> restUtil = getRestUtil();
        return restUtil.putForCore(url, request, bodyType, headers);
    }

    public static IResponse<Map> getForMap(String url) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.getForCore(url, mapType, null, null);
    }

    public static IResponse<Map> getForMap(String url, Map<String, ?> uriVariables) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.getForCore(url, mapType, uriVariables, null);
    }

    public static IResponse<Map> getForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.getForCore(url, mapType, uriVariables, headers);
    }

    public static IResponse<Map> deleteForMap(String url) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.deleteForCore(url, mapType, null, null);
    }

    public static IResponse<Map> deleteForMap(String url, Map<String, ?> uriVariables) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.deleteForCore(url, mapType, uriVariables, null);
    }

    public static IResponse<Map> deleteForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.deleteForCore(url, mapType, uriVariables, headers);
    }

    public static IResponse<Map> postForMap(String url, Object request) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.postForCore(url, request, mapType, null);
    }

    public static IResponse<Map> postForMap(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.postForCore(url, request, mapType, headers);
    }

    public static IResponse<Map> putForMap(String url, Object request) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.putForCore(url, request, mapType, null);
    }

    public static IResponse<Map> putForMap(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractRestUtils<IResponse<Map>> restUtil = getRestUtil();
        Type mapType = getMapType();
        return restUtil.putForCore(url, request, mapType, headers);
    }


    private static final class RestUtils<TBody> extends AbstractRestUtils<CmisAcqResponse<TBody>> {
        private RestUtils() {
        }

        @Override
        protected RestTemplate getRestTemplate() {
            return RestTemplateProvider.getRestTemplate();
        }

        @Override
        protected CmisAcqResponse<TBody> createResponse(String retFlag, String retMsg) {
            return CmisAcqResponse.create(retFlag, retMsg);
        }
    }
}
