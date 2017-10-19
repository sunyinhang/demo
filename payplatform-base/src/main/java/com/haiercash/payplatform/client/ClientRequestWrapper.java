package com.haiercash.payplatform.client;

import com.haiercash.payplatform.trace.OutgoingLog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;

import java.io.IOException;
import java.net.URI;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientRequestWrapper extends AbstractClientHttpRequest {
    private final AbstractClientHttpRequest clientHttpRequest;
    private ClientOutputStreamWrapper outputStream;

    public ClientRequestWrapper(AbstractClientHttpRequest clientHttpRequest) {
        this.clientHttpRequest = clientHttpRequest;
    }

    @Override
    public HttpMethod getMethod() {
        return this.clientHttpRequest.getMethod();
    }

    @Override
    public URI getURI() {
        return this.clientHttpRequest.getURI();
    }

    @Override
    public ClientOutputStreamWrapper getBodyInternal(HttpHeaders httpHeaders) throws IOException {
        if (this.outputStream == null)
            this.outputStream = new ClientOutputStreamWrapper(this.clientHttpRequest.getBody());
        return this.outputStream;
    }

    @Override
    protected ClientResponseWrapper executeInternal(HttpHeaders httpHeaders) throws IOException {
        this.clientHttpRequest.getHeaders().putAll(httpHeaders);
        StringBuilder builder = OutgoingLog.writeRequestLog(this);
        long begin = System.currentTimeMillis();
        ClientResponseWrapper responseWrapper;
        try {
            responseWrapper = new ClientResponseWrapper(this.clientHttpRequest.execute());
        } catch (Exception e) {
            OutgoingLog.writeError(builder, e, System.currentTimeMillis() - begin);
            throw e;
        }
        OutgoingLog.writeResponseLog(builder, responseWrapper, System.currentTimeMillis() - begin);
        return responseWrapper;
    }
}
