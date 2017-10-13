package com.haiercash.payplatform.rest;

import com.bestvike.collection.MapUtils;
import com.bestvike.lang.Convert;
import com.bestvike.lang.ThrowableUtils;
import com.haiercash.payplatform.utils.ConstUtil;
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
public abstract class AbstractRestUtil<TResponse extends IResponse> implements IRestUtil<TResponse> {
    private static final String ERROR_SERVER = ConstUtil.ERROR_CODE;
    private static final String ERROR_SERVER_MSG = "外部服务发生错误:HTTP-%s";
    private static final String ERROR_NULL = ConstUtil.ERROR_CODE;
    private static final String ERROR_NULL_MSG = "外部服务未返回任何数据";
    private static final String ERROR_UNKNOWN = ConstUtil.ERROR_CODE;
    private static final String ERROR_UNKNOWN_MSG = "通信异常:%s";
    private Class<?> responseRawType;//TResponse 的非泛型类型

    public AbstractRestUtil() {
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

    protected abstract TResponse createResponse(String retFlag, String retMsg);

    protected final ParameterizedTypeRef<TResponse> createResponseTypeRef(Type bodyType) {
        ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(this.responseRawType, new Type[]{bodyType}, null);
        return new ParameterizedTypeRef<>(parameterizedType);
    }

    @Override
    public TResponse getForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
            if (MapUtils.isNotEmpty(uriVariables)) {
                for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                    uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
            }
            URI uri = uriBuilder.build().encode().toUri();
            ParameterizedTypeRef<TResponse> responseTypeRef = this.createResponseTypeRef(bodyType);
            RestTemplate restTemplate = this.getRestTemplate();
            ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), responseTypeRef);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    @Override
    public TResponse deleteForCore(String url, Type bodyType, Map<String, ?> uriVariables, MultiValueMap<String, String> headers) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
            if (MapUtils.isNotEmpty(uriVariables)) {
                for (Map.Entry<String, ?> entry : uriVariables.entrySet())
                    uriBuilder.queryParam(entry.getKey(), Convert.toStringHuman(entry.getValue()));
            }
            URI uri = uriBuilder.build().encode().toUri();
            ParameterizedTypeRef<TResponse> responseTypeRef = this.createResponseTypeRef(bodyType);
            RestTemplate restTemplate = this.getRestTemplate();
            ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers), responseTypeRef);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    @Override
    public TResponse postForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
            URI uri = uriBuilder.build().encode().toUri();
            ParameterizedTypeRef<TResponse> responseTypeRef = this.createResponseTypeRef(bodyType);
            RestTemplate restTemplate = this.getRestTemplate();
            ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), responseTypeRef);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }

    @Override
    public TResponse putForCore(String url, Object request, Type bodyType, MultiValueMap<String, String> headers) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
            URI uri = uriBuilder.build().encode().toUri();
            ParameterizedTypeRef<TResponse> responseTypeRef = this.createResponseTypeRef(bodyType);
            RestTemplate restTemplate = this.getRestTemplate();
            ResponseEntity<TResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(request, headers), responseTypeRef);
            return (responseEntity.getStatusCode() == HttpStatus.OK)
                    ? (responseEntity.getBody() == null ? this.createResponse(ERROR_NULL, ERROR_NULL_MSG) : responseEntity.getBody())
                    : this.createResponse(ERROR_SERVER, String.format(ERROR_SERVER_MSG, responseEntity.getStatusCodeValue()));
        } catch (Exception e) {
            return this.createResponse(ERROR_UNKNOWN, String.format(ERROR_UNKNOWN_MSG, ThrowableUtils.getMessage(e)));
        }
    }
}
