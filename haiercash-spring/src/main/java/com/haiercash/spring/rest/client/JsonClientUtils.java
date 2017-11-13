package com.haiercash.spring.rest.client;

import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.rest.AbstractClientUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-31.
 */
public class JsonClientUtils {
    private static final ClientUtils CLIENT_UTILS = new ClientUtils();

    private JsonClientUtils() {
    }

    private static AbstractClientUtils getClientUtils() {
        return CLIENT_UTILS;
    }

    public static String getForString(String url) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.getForStringCore(url, null, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.getForStringCore(url, uriVariables, null);
    }

    public static String getForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.getForStringCore(url, uriVariables, headers);
    }

    public static String deleteForString(String url) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.deleteForStringCore(url, null, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.deleteForStringCore(url, uriVariables, null);
    }

    public static String deleteForString(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.deleteForStringCore(url, uriVariables, headers);
    }

    public static String postForString(String url, Object request) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.postForStringCore(url, request, null);
    }

    public static String postForString(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.postForStringCore(url, request, headers);
    }

    public static String putForString(String url, Object request) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.putForStringCore(url, request, null);
    }

    public static String putForString(String url, Object request, MultiValueMap<String, String> headers) {
        AbstractClientUtils clientUtils = getClientUtils();
        return clientUtils.putForStringCore(url, request, headers);
    }


    private static final class ClientUtils extends AbstractClientUtils {
        private ClientUtils() {
        }

        @Override
        protected RestTemplate getRestTemplate() {
            return RestTemplateProvider.getRestTemplateJson();
        }
    }
}
