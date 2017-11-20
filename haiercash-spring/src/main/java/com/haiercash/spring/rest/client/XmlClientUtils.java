package com.haiercash.spring.rest.client;

import com.haiercash.core.reflect.GenericType;
import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.rest.AbstractClientUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-31.
 */
public class XmlClientUtils {
    private static final ClientUtils CLIENT_UTILS = new ClientUtils();
    private static final ParameterizedTypeImpl MAP_STRING_OBJECT_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);

    private XmlClientUtils() {
    }

    private static Type getMapType() {
        return MAP_STRING_OBJECT_TYPE;
    }

    private static <TResponse> AbstractClientUtils<TResponse> getClientUtils() {
        return CLIENT_UTILS;
    }

    public static <TResponse> TResponse getForObject(String url, Class<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.getForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse getForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.getForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse getForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.getForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse deleteForObject(String url, Class<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.deleteForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.deleteForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, Class<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.deleteForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, Class<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.postForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, Class<TResponse> responseType, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.postForCore(url, request, responseType, headers);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, Class<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.putForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, Class<TResponse> responseType, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.putForCore(url, request, responseType, headers);
    }

    public static <TResponse> TResponse getForObject(String url, GenericType<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.getForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse getForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.getForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse getForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.getForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse deleteForObject(String url, GenericType<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.deleteForCore(url, responseType, null, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.deleteForCore(url, responseType, uriVariables, null);
    }

    public static <TResponse> TResponse deleteForObject(String url, GenericType<TResponse> responseType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.deleteForCore(url, responseType, uriVariables, headers);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, GenericType<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.postForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse postForObject(String url, Object request, GenericType<TResponse> responseType, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.postForCore(url, request, responseType, headers);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, GenericType<TResponse> responseType) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.putForCore(url, request, responseType, null);
    }

    public static <TResponse> TResponse putForObject(String url, Object request, GenericType<TResponse> responseType, MultiValueMap<String, String> headers) {
        AbstractClientUtils<TResponse> clientUtils = getClientUtils();
        return clientUtils.putForCore(url, request, responseType, headers);
    }

    public static Map<String, Object> getForMap(String url) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.getForCore(url, mapType, null, null);
    }

    public static Map<String, Object> getForMap(String url, Map<String, ?> uriVariables) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.getForCore(url, mapType, uriVariables, null);
    }

    public static Map<String, Object> getForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.getForCore(url, mapType, uriVariables, headers);
    }

    public static Map<String, Object> deleteForMap(String url) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.deleteForCore(url, mapType, null, null);
    }

    public static Map<String, Object> deleteForMap(String url, Map<String, ?> uriVariables) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.deleteForCore(url, mapType, uriVariables, null);
    }

    public static Map<String, Object> deleteForMap(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.deleteForCore(url, mapType, uriVariables, headers);
    }

    public static Map<String, Object> postForMap(String url, Object request) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.postForCore(url, request, mapType, null);
    }

    public static Map<String, Object> postForMap(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.postForCore(url, request, mapType, headers);
    }

    public static Map<String, Object> putForMap(String url, Object request) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.putForCore(url, request, mapType, null);
    }

    public static Map<String, Object> putForMap(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractClientUtils<Map<String, Object>> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.putForCore(url, request, mapType, headers);
    }

    public static String getForString(String url) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.getForCore(url, mapType, null, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.getForCore(url, mapType, uriVariables, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.getForCore(url, mapType, uriVariables, headers);
    }

    public static String deleteForString(String url) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.deleteForCore(url, mapType, null, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.deleteForCore(url, mapType, uriVariables, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.deleteForCore(url, mapType, uriVariables, headers);
    }

    public static String postForString(String url, Object request) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.postForCore(url, request, mapType, null);
    }

    public static String postForString(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.postForCore(url, request, mapType, headers);
    }

    public static String putForString(String url, Object request) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.putForCore(url, request, mapType, null);
    }

    public static String putForString(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractClientUtils<String> clientUtils = getClientUtils();
        Type mapType = getMapType();
        return clientUtils.putForCore(url, request, mapType, headers);
    }


    private static final class ClientUtils<TResponse> extends AbstractClientUtils<TResponse> {
        private ClientUtils() {
        }

        @Override
        protected RestTemplate getRestTemplate() {
            return RestTemplateProvider.getRestTemplateXml();
        }
    }
}
