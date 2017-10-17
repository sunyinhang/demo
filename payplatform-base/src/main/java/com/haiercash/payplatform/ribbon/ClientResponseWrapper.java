package com.haiercash.payplatform.ribbon;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse clientHttpResponse;
    private ClientInputStreamWrapper inputStream;

    public ClientResponseWrapper(ClientHttpResponse clientHttpResponse) {
        this.clientHttpResponse = clientHttpResponse;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return this.clientHttpResponse.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return this.clientHttpResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.clientHttpResponse.getStatusText();
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.clientHttpResponse.getHeaders();
    }

    @Override
    public ClientInputStreamWrapper getBody() throws IOException {
        if (this.inputStream == null)
            this.inputStream = new ClientInputStreamWrapper(this.clientHttpResponse.getBody());
        return this.inputStream;
    }

    @Override
    public void close() {
        this.clientHttpResponse.close();
    }
}
