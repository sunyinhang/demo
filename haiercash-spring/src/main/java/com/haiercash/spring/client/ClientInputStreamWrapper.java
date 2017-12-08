package com.haiercash.spring.client;

import com.haiercash.core.io.IOUtils;
import com.haiercash.spring.trace.TraceConfig;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientInputStreamWrapper extends InputStream {
    private final InputStream inputStream;
    private byte[] cachedBuffer;
    private int cachedIndex;
    private int cachedLength;
    private String content;

    public ClientInputStreamWrapper(InputStream inputStream) {
        Assert.notNull(inputStream, "inputStream can not be null.");
        this.inputStream = inputStream;
        this.cacheStream();
        this.parseBody();
    }

    private void cacheStream() {
        try {
            this.cachedBuffer = TraceConfig.BUFFER.get();
            this.cachedLength = IOUtils.read(this.inputStream, this.cachedBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseBody() {
        try {
            this.content = this.cachedLength > TraceConfig.DISPLAY_SIZE
                    ? (new String(this.cachedBuffer, 0, TraceConfig.DISPLAY_SIZE, TraceConfig.DEFAULT_CHARSET) + TraceConfig.BODY_OVER_FLOW)
                    : new String(this.cachedBuffer, 0, this.cachedLength, TraceConfig.DEFAULT_CHARSET);
        } catch (Exception e) {
            this.content = TraceConfig.BODY_PARSE_FAIL;
        }
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public int read() throws IOException {
        return this.cachedIndex == this.cachedLength
                ? this.inputStream.read()
                : (this.cachedBuffer[this.cachedIndex++] & 255);
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
        this.cachedBuffer = null;
    }
}
