package com.haiercash.spring.rest;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.reflect.GenericTypeReference;
import com.haiercash.spring.utils.ConstUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public abstract class AbstractRestUtils<TResponse extends IResponse> implements IRestUtils<TResponse> {
    private static final String ERROR_SERVER = ConstUtil.ERROR_CODE;
    private static final String ERROR_SERVER_MSG = "外部服务发生错误:HTTP-%s";
    private static final String ERROR_NULL = ConstUtil.ERROR_CODE;
    private static final String ERROR_NULL_MSG = "外部服务未返回任何数据";
    private Class<?> responseRawType;//TResponse 的非泛型类型

    public AbstractRestUtils() {
        ParameterizedType superClass = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type firstActualTypeArgument = superClass.getActualTypeArguments()[0];
        if (firstActualTypeArgument instanceof Class)
            this.responseRawType = (Class<?>) firstActualTypeArgument;
        else if (firstActualTypeArgument instanceof ParameterizedTypeImpl)
            this.responseRawType = ((ParameterizedTypeImpl) firstActualTypeArgument).getRawType();
        else
            throw new RuntimeException("can not find raw type of first actual type argument.");
    }

    protected abstract RestTemplate getRestTemplate();

    protected abstract TResponse fail(String retFlag, String retMsg);

    protected final GenericTypeReference<TResponse> createResponseTypeReference(Type bodyType) {
        ParameterizedTypeImpl responseType = ParameterizedTypeImpl.make(this.responseRawType, new Type[]{bodyType}, null);
        return new GenericTypeReference<>(responseType);
    }

    @Override
    public TResponse getForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), responseTypeReference);
        return (responseEntity.getStatusCode() == HttpStatus.OK)
                ? (responseEntity.getBody() == null ? this.fail(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                : this.fail(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    @Override
    public TResponse deleteForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        if (MapUtils.isNotEmpty(uriVariables)) {
            for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
        }
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), responseTypeReference);
        return (responseEntity.getStatusCode() == HttpStatus.OK)
                ? (responseEntity.getBody() == null ? this.fail(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                : this.fail(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    @Override
    public TResponse postForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), responseTypeReference);
        return (responseEntity.getStatusCode() == HttpStatus.OK)
                ? (responseEntity.getBody() == null ? this.fail(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                : this.fail(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }

    @Override
    public TResponse putForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
        URI uri = uriBuilder.build().encode().toUri();
        GenericTypeReference<TResponse> responseTypeReference = this.createResponseTypeReference(bodyType);
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(request, headers), responseTypeReference);
        return (responseEntity.getStatusCode() == HttpStatus.OK)
                ? (responseEntity.getBody() == null ? this.fail(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                : this.fail(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
    }
}
