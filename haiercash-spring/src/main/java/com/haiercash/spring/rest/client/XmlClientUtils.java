package com.haiercash.spring.rest.client;

import com.haiercash.core.reflect.GenericType;
import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.rest.AbstractRestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-31.
 */
public final class XmlClientUtils {
    private static final RestUtils REST_UTILS = new RestUtils();
    private static final Type MAP_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);
    private static final Type LIST_TYPE = ParameterizedTypeImpl.make(List.class, new Type[]{Object.class}, null);
    private static final Type STRING_TYPE = String.class;

    private XmlClientUtils() {
    }

    public static <TResponse> TResponse getForObject(String url, Class<TResponse> responseType) {
        return REST_UTILS.getForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse getForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse getForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse deleteForObject(String url, Class<TResponse> responseType) {
        return REST_UTILS.deleteForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, Class<TResponse> responseType) {
        return REST_UTILS.postForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, Class<TResponse> responseType, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, responseType, headers);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, Class<TResponse> responseType) {
        return REST_UTILS.putForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, Class<TResponse> responseType, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, responseType, headers);
    }

    public static <TResponse> TResponse getForObject(String url, GenericType<TResponse> responseType) {
        return REST_UTILS.getForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse getForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse getForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse deleteForObject(String url, GenericType<TResponse> responseType) {
        return REST_UTILS.deleteForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, GenericType<TResponse> responseType) {
        return REST_UTILS.postForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, GenericType<TResponse> responseType, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, responseType, headers);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, GenericType<TResponse> responseType) {
        return REST_UTILS.putForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, GenericType<TResponse> responseType, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, responseType, headers);
    }

    public static Map<String, Object> getForMap(String url) {
        return REST_UTILS.getForCore(url, MAP_TYPE, null, null);
    }

    public static Map<String, Object> getForMap(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, MAP_TYPE, uriVariables, null);
    }

    public static Map<String, Object> getForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, MAP_TYPE, uriVariables, headers);
    }

    public static Map<String, Object> deleteForMap(String url) {
        return REST_UTILS.deleteForCore(url, MAP_TYPE, null, null);
    }

    public static Map<String, Object> deleteForMap(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, MAP_TYPE, uriVariables, null);
    }

    public static Map<String, Object> deleteForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, MAP_TYPE, uriVariables, headers);
    }

    public static Map<String, Object> postForMap(String url, Object request) {
        return REST_UTILS.postForCore(url, request, MAP_TYPE, null);
    }

    public static Map<String, Object> postForMap(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, MAP_TYPE, headers);
    }

    public static Map<String, Object> putForMap(String url, Object request) {
        return REST_UTILS.putForCore(url, request, MAP_TYPE, null);
    }

    public static Map<String, Object> putForMap(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, MAP_TYPE, headers);
    }

    public static List<Object> getForList(String url) {
        return REST_UTILS.getForCore(url, LIST_TYPE, null, null);
    }

    public static List<Object> getForList(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.getForCore(url, LIST_TYPE, uriVariables, null);
    }

    public static List<Object> getForList(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.getForCore(url, LIST_TYPE, uriVariables, headers);
    }

    public static List<Object> deleteForList(String url) {
        return REST_UTILS.deleteForCore(url, LIST_TYPE, null, null);
    }

    public static List<Object> deleteForList(String url, Map<String, ?> uriVariables) {
        return REST_UTILS.deleteForCore(url, LIST_TYPE, uriVariables, null);
    }

    public static List<Object> deleteForList(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        return REST_UTILS.deleteForCore(url, LIST_TYPE, uriVariables, headers);
    }

    public static List<Object> postForList(String url, Object request) {
        return REST_UTILS.postForCore(url, request, LIST_TYPE, null);
    }

    public static List<Object> postForList(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, request, LIST_TYPE, headers);
    }

    public static List<Object> putForList(String url, Object request) {
        return REST_UTILS.putForCore(url, request, LIST_TYPE, null);
    }

    public static List<Object> putForList(String url, Object request, MultiValueMap<String, String> headers) {
        return REST_UTILS.putForCore(url, request, LIST_TYPE, headers);
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
            return RestTemplateProvider.getRestTemplateXml();
        }
    }
}
