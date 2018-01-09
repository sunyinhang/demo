package com.haiercash.spring.rest.acq;

import com.haiercash.core.reflect.GenericType;
import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.rest.AbstractRestUtils;
import com.haiercash.spring.rest.IResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public final class AcqRestUtils {
    private static final RestUtils REST_UTILS = new RestUtils();
    private static final Type MAP_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);
    private static final Type STRING_TYPE = String.class;

    private AcqRestUtils() {
    }

    private static Type getResponseType(Type bodyType) {
        return ParameterizedTypeImpl.make(AcqResponse.class, new Type[]{bodyType}, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType) {
        return REST_UTILS.getForCore(url, getResponseType(bodyType), null, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType) {
        return REST_UTILS.deleteForCore(url, getResponseType(bodyType), null, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, Class<TBody> bodyType) {
        return REST_UTILS.postForCore(url, request, getResponseType(bodyType), null);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, getResponseType(bodyType), headers);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, Class<TBody> bodyType) {
        return REST_UTILS.putForCore(url, request, getResponseType(bodyType), null);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, getResponseType(bodyType), headers);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType) {
        return REST_UTILS.getForCore(url, getResponseType(bodyType), null, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType) {
        return REST_UTILS.deleteForCore(url, getResponseType(bodyType), null, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, null);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, headers);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, GenericType<TBody> bodyType) {
        return REST_UTILS.postForCore(url, request, getResponseType(bodyType), null);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, getResponseType(bodyType), headers);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, GenericType<TBody> bodyType) {
        return REST_UTILS.putForCore(url, request, getResponseType(bodyType), null);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, getResponseType(bodyType), headers);
    }

    public static IResponse<Map> getForMap(String url) {
        return REST_UTILS.getForCore(url, getResponseType(MAP_TYPE), null, null);
    }

    public static IResponse<Map> getForMap(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, getResponseType(MAP_TYPE), uriVariables, null);
    }

    public static IResponse<Map> getForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, getResponseType(MAP_TYPE), uriVariables, headers);
    }

    public static IResponse<Map> deleteForMap(String url) {
        return REST_UTILS.deleteForCore(url, getResponseType(MAP_TYPE), null, null);
    }

    public static IResponse<Map> deleteForMap(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, getResponseType(MAP_TYPE), uriVariables, null);
    }

    public static IResponse<Map> deleteForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, getResponseType(MAP_TYPE), uriVariables, headers);
    }

    public static IResponse<Map> postForMap(String url, Object request) {
        return REST_UTILS.postForCore(url, request, getResponseType(MAP_TYPE), null);
    }

    public static IResponse<Map> postForMap(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, getResponseType(MAP_TYPE), headers);
    }

    public static IResponse<Map> putForMap(String url, Object request) {
        return REST_UTILS.putForCore(url, request, getResponseType(MAP_TYPE), null);
    }

    public static IResponse<Map> putForMap(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, getResponseType(MAP_TYPE), headers);
    }

    public static String getForString(String url) {
        return REST_UTILS.getForCore(url, STRING_TYPE, null, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, STRING_TYPE, uriVariables, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, STRING_TYPE, uriVariables, headers);
    }

    public static String deleteForString(String url) {
        return REST_UTILS.deleteForCore(url, STRING_TYPE, null, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, STRING_TYPE, uriVariables, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, STRING_TYPE, uriVariables, headers);
    }

    public static String postForString(String url, Object request) {
        return REST_UTILS.postForCore(url, request, STRING_TYPE, null);
    }

    public static String postForString(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, STRING_TYPE, headers);
    }

    public static String putForString(String url, Object request) {
        return REST_UTILS.putForCore(url, request, STRING_TYPE, null);
    }

    public static String putForString(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, STRING_TYPE, headers);
    }


    private static final class RestUtils extends AbstractRestUtils {
        private RestUtils() {
        }

        @Override
        protected RestTemplate getRestTemplate() {
            return RestTemplateProvider.getRestTemplate();
        }
    }
}
