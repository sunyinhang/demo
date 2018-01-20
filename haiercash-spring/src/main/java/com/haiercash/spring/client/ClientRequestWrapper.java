package com.haiercash.spring.client;

import com.haiercash.spring.trace.rest.OutgoingLog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientRequestWrapper extends AbstractClientHttpRequest {
    private final AbstractClientHttpRequest request;
    private ClientOutputStreamWrapper outputStream;

    public ClientRequestWrapper(AbstractClientHttpRequest request) {
        this.request = request;
    }

    @Override
    public HttpMethod getMethod() {
        return this.request.getMethod();
    }

    @Override
    public URI getURI() {
        return this.request.getURI();
    }

    @Override
    public ClientOutputStreamWrapper getBodyInternal(HttpHeaders headers) throws IOException {
        if (this.outputStream == null)
            this.outputStream = new ClientOutputStreamWrapper(this.request.getBody());
        return this.outputStream;
    }

    @Override
    protected ClientResponseWrapper executeInternal(HttpHeaders headers) throws IOException {
        this.request.getHeaders().putAll(headers);
        Map<String, Object> log = OutgoingLog.writeBeginLog(this);
        long begin = System.currentTimeMillis();
        ClientResponseWrapper response;
        try {
            response = new ClientResponseWrapper(this.request.execute());
        } catch (Exception e) {
            OutgoingLog.writeErrorLog(log, e, System.currentTimeMillis() - begin);
            throw e;
        }
        OutgoingLog.writeEndLog(log, response, System.currentTimeMillis() - begin);
        return response;
    }
}
