package com.haiercash.spring.rest.common;

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
public final class CommonRestUtils {
    private static final RestUtils REST_UTILS = new RestUtils();
    private static final Type MAP_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);
    private static final Type STRING_TYPE = String.class;

    private CommonRestUtils() {
    }

    private static Type getResponseType(Type bodyType) {
        return ParameterizedTypeImpl.make(CommonResponse.class, new Type[]{bodyType}, null);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.getForCore(url, getResponseType(bodyType), null, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables) {
        IResponse<TBody> response = REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.deleteForCore(url, getResponseType(bodyType), null, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables) {
        IResponse<TBody> response = REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, Class<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, Class<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, Class<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.putForCore(url, request, getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.putForCore(url, request, getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.getForCore(url, getResponseType(bodyType), null, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables) {
        IResponse<TBody> response = REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> getForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.getForCore(url, getResponseType(bodyType), uriVariables, headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.deleteForCore(url, getResponseType(bodyType), null, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables) {
        IResponse<TBody> response = REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> deleteForObject(String url, GenericType<TBody> bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.deleteForCore(url, getResponseType(bodyType), uriVariables, headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, GenericType<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, Object request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, GenericType<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.putForCore(url, request, getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> putForObject(String url, Object request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.putForCore(url, request, getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static IResponse<Map> getForMap(String url) {
        IResponse<Map> response = REST_UTILS.getForCore(url, getResponseType(MAP_TYPE), null, null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> getForMap(String url, Map<String, ?> uriVariables) {
        IResponse<Map> response = REST_UTILS.getForCore(url, getResponseType(MAP_TYPE), uriVariables, null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> getForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        IResponse<Map> response = REST_UTILS.getForCore(url, getResponseType(MAP_TYPE), uriVariables, headers);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> deleteForMap(String url) {
        IResponse<Map> response = REST_UTILS.deleteForCore(url, getResponseType(MAP_TYPE), null, null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> deleteForMap(String url, Map<String, ?> uriVariables) {
        IResponse<Map> response = REST_UTILS.deleteForCore(url, getResponseType(MAP_TYPE), uriVariables, null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> deleteForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        IResponse<Map> response = REST_UTILS.deleteForCore(url, getResponseType(MAP_TYPE), uriVariables, headers);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> postForMap(String url, Object request) {
        IResponse<Map> response = REST_UTILS.postForCore(url, request, getResponseType(MAP_TYPE), null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> postForMap(String url, Object request, MultiValueMap<String, String> headers) {
        IResponse<Map> response = REST_UTILS.postForCore(url, request, getResponseType(MAP_TYPE), headers);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> putForMap(String url, Object request) {
        IResponse<Map> response = REST_UTILS.putForCore(url, request, getResponseType(MAP_TYPE), null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> putForMap(String url, Object request, MultiValueMap<String, String> headers) {
        IResponse<Map> response = REST_UTILS.putForCore(url, request, getResponseType(MAP_TYPE), headers);
        return response.afterPropertiesSet(MAP_TYPE);
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
