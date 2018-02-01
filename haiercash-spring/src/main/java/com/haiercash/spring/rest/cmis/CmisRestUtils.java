package com.haiercash.spring.rest.cmis;

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
public final class CmisRestUtils {
    private static final RestUtils REST_UTILS = new RestUtils();
    private static final Type MAP_TYPE = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Object.class}, null);
    private static final Type LIST_TYPE = ParameterizedTypeImpl.make(List.class, new Type[]{Object.class}, null);
    private static final Type STRING_TYPE = String.class;

    private CmisRestUtils() {
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest request, Class<TBody> bodyType) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, version.getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest request, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, version.getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest request, GenericType<TBody> bodyType) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, version.getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest request, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<TBody> response = REST_UTILS.postForCore(url, request, version.getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static IResponse<Map> postForMap(ICmisRequest request) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<Map> response = REST_UTILS.postForCore(url, request, version.getResponseType(MAP_TYPE), null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> postForMap(ICmisRequest request, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<Map> response = REST_UTILS.postForCore(url, request, version.getResponseType(MAP_TYPE), headers);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<List> postForList(ICmisRequest request) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<List> response = REST_UTILS.postForCore(url, request, version.getResponseType(LIST_TYPE), null);
        return response.afterPropertiesSet(LIST_TYPE);
    }

    public static IResponse<List> postForList(ICmisRequest request, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        IResponse<List> response = REST_UTILS.postForCore(url, request, version.getResponseType(LIST_TYPE), headers);
        return response.afterPropertiesSet(LIST_TYPE);
    }

    public static String postForString(ICmisRequest request) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        return REST_UTILS.postForCore(url, request, STRING_TYPE, null);
    }

    public static String postForString(ICmisRequest request, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(request.getTradeCode());
        String url = version.getUrl(request);
        return REST_UTILS.postForCore(url, request, STRING_TYPE, headers);
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
