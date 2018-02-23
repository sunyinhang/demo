package com.haiercash.spring.rest.acq;

import com.haiercash.core.reflect.GenericType;
import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.rest.AbstractRestUtils;
import com.haiercash.spring.rest.IResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public final class AcqRestUtils {
    private static final RestUtils REST_UTILS = new RestUtils();
    private static final Type MAP_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);
    private static final Type LIST_TYPE = ParameterizedTypeImpl.make(List.class, new Type[]{Object.class}, null);
    private static final Type STRING_TYPE = String.class;

    private AcqRestUtils() {
    }

    private static Type getResponseType(Type bodyType) {
        return ParameterizedTypeImpl.make(AcqResponse.class, new Type[]{bodyType}, null);
    }

    public static <TBody> IResponse<TBody> postForObject(String url, IAcqRequest body, Class<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, getResponseType(bodyType), null);
        response.afterPropertiesSet(bodyType);
        return response;
    }

    public static <TBody> IResponse<TBody> postForObject(String url, IAcqRequest body, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, getResponseType(bodyType), headers);
        response.afterPropertiesSet(bodyType);
        return response;
    }

    public static <TBody> IResponse<TBody> postForObject(String url, IAcqRequest body, GenericType<TBody> bodyType) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, getResponseType(bodyType), null);
        response.afterPropertiesSet(bodyType);
        return response;
    }

    public static <TBody> IResponse<TBody> postForObject(String url, IAcqRequest body, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, getResponseType(bodyType), headers);
        response.afterPropertiesSet(bodyType);
        return response;
    }

    public static IResponse<Map> postForMap(String url, IAcqRequest body) {
        IResponse<Map> response = REST_UTILS.postForCore(url, body, getResponseType(MAP_TYPE), null);
        response.afterPropertiesSet(MAP_TYPE);
        return response;
    }

    public static IResponse<Map> postForMap(String url, IAcqRequest body, MultiValueMap<String, String> headers) {
        IResponse<Map> response = REST_UTILS.postForCore(url, body, getResponseType(MAP_TYPE), headers);
        response.afterPropertiesSet(MAP_TYPE);
        return response;
    }

    public static IResponse<List> postForList(String url, IAcqRequest body) {
        IResponse<List> response = REST_UTILS.postForCore(url, body, getResponseType(LIST_TYPE), null);
        response.afterPropertiesSet(LIST_TYPE);
        return response;
    }

    public static IResponse<List> postForList(String url, IAcqRequest body, MultiValueMap<String, String> headers) {
        IResponse<List> response = REST_UTILS.postForCore(url, body, getResponseType(LIST_TYPE), headers);
        response.afterPropertiesSet(LIST_TYPE);
        return response;
    }

    public static String postForString(String url, IAcqRequest body) {
        return REST_UTILS.postForCore(url, body, STRING_TYPE, null);
    }

    public static String postForString(String url, IAcqRequest body, MultiValueMap<String, String> headers) {
        return REST_UTILS.postForCore(url, body, STRING_TYPE, headers);
    }


    private static final class RestUtils extends AbstractRestUtils {
        private RestUtils() {
        }

        @Override
        protected RestTemplate getRestTemplate() {
            return RestTemplateProvider.getRibbonJsonRestTemplate();
        }
    }
}
