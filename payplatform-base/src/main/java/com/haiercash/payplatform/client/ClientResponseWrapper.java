package com.haiercash.payplatform.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse response;
    private ClientInputStreamWrapper inputStream;

    public ClientResponseWrapper(ClientHttpResponse response) {
        this.response = response;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return this.response.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return this.response.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.response.getStatusText();
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.response.getHeaders();
    }

    @Override
    public ClientInputStreamWrapper getBody() throws IOException {
        if (this.inputStream == null)
            this.inputStream = new ClientInputStreamWrapper(this.response.getBody());
        return this.inputStream;
    }

    @Override
    public void close() {
        this.response.close();
    }
}
