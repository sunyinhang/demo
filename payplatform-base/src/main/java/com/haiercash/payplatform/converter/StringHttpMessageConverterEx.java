package com.haiercash.payplatform.converter;

import com.bestvike.io.CharsetNames;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-10-30.
 */
public final class StringHttpMessageConverterEx extends AbstractHttpMessageConverter<String> {
    private static final Charset DEFAULT_CHARSET = Charset.forName(CharsetNames.UTF_8);
    private volatile List<Charset> availableCharsets;

    public StringHttpMessageConverterEx(MediaType supportedMediaType) {
        super(DEFAULT_CHARSET, supportedMediaType, MediaType.ALL);
    }

    private Charset getContentTypeCharset(MediaType contentType) {
        return contentType != null && contentType.getCharset() != null ? contentType.getCharset() : this.getDefaultCharset();
    }

    private List<Charset> getAcceptedCharsets() {
        if (this.availableCharsets == null)
            this.availableCharsets = new ArrayList<>(Charset.availableCharsets().values());
        return this.availableCharsets;
    }

    @Override
    protected Long getContentLength(String str, MediaType contentType) {
        Charset charset = this.getContentTypeCharset(contentType);
        try {
            return (long) str.getBytes(charset.name()).length;
        } catch (UnsupportedEncodingException var5) {
            throw new IllegalStateException(var5);
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class == clazz;
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
        Charset charset = this.getContentTypeCharset(inputMessage.getHeaders().getContentType());
        return StreamUtils.copyToString(inputMessage.getBody(), charset);
    }

    @Override
    protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException {
        outputMessage.getHeaders().setAcceptCharset(this.getAcceptedCharsets());
        Charset charset = this.getContentTypeCharset(outputMessage.getHeaders().getContentType());
        StreamUtils.copy(str, charset, outputMessage.getBody());
    }
}
