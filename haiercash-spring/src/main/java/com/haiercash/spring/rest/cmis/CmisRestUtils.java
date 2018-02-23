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

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest body, Class<TBody> bodyType) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, version.getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest body, Class<TBody> bodyType, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, version.getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest body, GenericType<TBody> bodyType) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, version.getResponseType(bodyType), null);
        return response.afterPropertiesSet(bodyType);
    }

    public static <TBody> IResponse<TBody> postForObject(ICmisRequest body, GenericType<TBody> bodyType, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<TBody> response = REST_UTILS.postForCore(url, body, version.getResponseType(bodyType), headers);
        return response.afterPropertiesSet(bodyType);
    }

    public static IResponse<Map> postForMap(ICmisRequest body) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<Map> response = REST_UTILS.postForCore(url, body, version.getResponseType(MAP_TYPE), null);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<Map> postForMap(ICmisRequest body, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<Map> response = REST_UTILS.postForCore(url, body, version.getResponseType(MAP_TYPE), headers);
        return response.afterPropertiesSet(MAP_TYPE);
    }

    public static IResponse<List> postForList(ICmisRequest body) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<List> response = REST_UTILS.postForCore(url, body, version.getResponseType(LIST_TYPE), null);
        return response.afterPropertiesSet(LIST_TYPE);
    }

    public static IResponse<List> postForList(ICmisRequest body, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        IResponse<List> response = REST_UTILS.postForCore(url, body, version.getResponseType(LIST_TYPE), headers);
        return response.afterPropertiesSet(LIST_TYPE);
    }

    public static String postForString(ICmisRequest body) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
        return REST_UTILS.postForCore(url, body, STRING_TYPE, null);
    }

    public static String postForString(ICmisRequest body, MultiValueMap<String, String> headers) {
        CmisVersion version = CmisVersion.forTradeCode(body.getTradeCode());
        String url = version.getUrl(body);
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
