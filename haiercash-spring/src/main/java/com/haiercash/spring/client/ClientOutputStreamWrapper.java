package com.haiercash.spring.client;

import com.haiercash.spring.trace.TraceConfig;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientOutputStreamWrapper extends OutputStream {
    private final OutputStream outputStream;
    private byte[] cachedBuffer;
    private int cachedLength;
    private boolean overFlow;
    private String content;

    public ClientOutputStreamWrapper(OutputStream outputStream) {
        Assert.notNull(outputStream, "outputStream can not be null.");
        this.outputStream = outputStream;
    }

    private String parseBody() {
        if (this.cachedBuffer == null)
            return null;
        try {
            return this.overFlow
                    ? new String(this.cachedBuffer, 0, this.cachedLength, TraceConfig.DEFAULT_CHARSET) + TraceConfig.BODY_OVER_FLOW
                    : new String(this.cachedBuffer, 0, this.cachedLength, TraceConfig.DEFAULT_CHARSET);
        } catch (Exception e) {
            return TraceConfig.BODY_PARSE_FAIL;
        }
    }

    public String getContent() {
        return this.content == null ? this.parseBody() : this.content;
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
        if (this.cachedBuffer == null)
            this.cachedBuffer = TraceConfig.BUFFER.get();
        if (this.cachedLength >= TraceConfig.DISPLAY_SIZE) {
            this.overFlow = true;
            return;
        }
        this.cachedBuffer[this.cachedLength++] = (byte) b;
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
        if (this.content == null)
            this.content = this.parseBody();
    }

    @Override
    public void close() throws IOException {
        this.flush();
        this.outputStream.close();
        this.cachedBuffer = null;
    }
}
