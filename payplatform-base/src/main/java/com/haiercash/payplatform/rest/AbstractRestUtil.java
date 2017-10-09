package com.haiercash.payplatform.rest;

import com.bestvike.lang.ThrowableUtils;
import com.haiercash.payplatform.utils.ConstUtil;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public abstract class AbstractRestUtil<TResponse extends IRestResponse> implements IRestUtil<TResponse> {
    private static final String ERROR_SERVER = ConstUtil.ERROR_CODE;
    private static final String ERROR_SERVER_MSG = "外部服务发生错误:HTTP-%s";
    private static final String ERROR_NULL = ConstUtil.ERROR_CODE;
    private static final String ERROR_NULL_MSG = "外部服务未返回任何数据";
    private static final String ERROR_UNKNOWN = ConstUtil.ERROR_CODE;
    private static final String ERROR_UNKNOWN_MSG = "通信异常:%s";

    protected abstract RestTemplate getRestTemplate();

    protected abstract TResponse createResponse(String retFlag, String retMsg);

    public TResponse getCore(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<TResponse> responseEntity = this.getRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<TResponse>() {
            }, uriVariables);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    public TResponse deleteCore(String url, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<TResponse> responseEntity = this.getRestTemplate().exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), new ParameterizedTypeReference<TResponse>() {
            }, uriVariables);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    public TResponse postCore(String url, Object request, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<TResponse> responseEntity = this.getRestTemplate().exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), new ParameterizedTypeReference<TResponse>() {
            });
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    public TResponse putCore(String url, Object request, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<TResponse> responseEntity = this.getRestTemplate().exchange(url, HttpMethod.PUT, new HttpEntity<>(request, headers), new ParameterizedTypeReference<TResponse>() {
            });
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }
}
