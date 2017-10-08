package com.haiercash.payplatform.rest;

import com.bestvike.lang.ThrowableUtils;
import com.haiercash.payplatform.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class AbstractRestUtil<T extends IRestResponse> implements IRestUtil<T> {
    private static final String ERROR_SERVER = ConstUtil.ERROR_CODE;
    private static final String ERROR_SERVER_MSG = "外部服务发生错误:HTTP-%s";
    private static final String ERROR_NULL = ConstUtil.ERROR_CODE;
    private static final String ERROR_NULL_MSG = "外部服务未返回任何数据";
    private static final String ERROR_UNKNOWN = ConstUtil.ERROR_CODE;
    private static final String ERROR_UNKNOWN_MSG = "通信异常:%s";

    @Autowired
    private RestTemplate restTemplate;
    private String instanceUrl;

    public AbstractRestUtil() {
        this.instanceUrl = this.instanceUrl();
    }

    private String getUrl(String api) {
        return this.instanceUrl + api;
    }

    protected abstract String instanceUrl();

    protected abstract T createResponse(String retFlag, String retMsg);

    public T getCore(String api, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<T> responseEntity = this.restTemplate.exchange(this.getUrl(api), HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<T>() {
            }, uriVariables);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    public T deleteCore(String api, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<T> responseEntity = this.restTemplate.exchange(this.getUrl(api), HttpMethod.DELETE, new HttpEntity<>(headers), new ParameterizedTypeReference<T>() {
            }, uriVariables);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    public T postCore(String api, Object request, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<T> responseEntity = this.restTemplate.exchange(this.getUrl(api), HttpMethod.POST, new HttpEntity<>(request, headers), new ParameterizedTypeReference<T>() {
            });
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    public T putCore(String api, Object request, MultiValueMap<String, String> headers) {
        try {
            ResponseEntity<T> responseEntity = this.restTemplate.exchange(this.getUrl(api), HttpMethod.PUT, new HttpEntity<>(request, headers), new ParameterizedTypeReference<T>() {
            });
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }
}
