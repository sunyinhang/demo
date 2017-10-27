package com.haiercash.payplatform.client;

import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.trace.TraceLogConfig;
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

    public String getContent() {
        return this.content;
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
        if (this.cachedBuffer == null)
            this.cachedBuffer = TraceLogConfig.BUFFER.get();
        if (this.cachedLength >= TraceLogConfig.MAX_DISPLAY) {
            this.overFlow = true;
            return;
        }
        this.cachedBuffer[this.cachedLength++] = (byte) b;
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
        //已经 flush
        if (this.content != null)
            return;
        if (this.cachedBuffer == null) {
            this.content = StringUtils.EMPTY;
            return;
        }
        try {
            this.content = this.overFlow
                    ? (new String(this.cachedBuffer, 0, this.cachedLength, TraceLogConfig.DEFAULT_CHARSET) + TraceLogConfig.BODY_OVER_FLOW)
                    : new String(this.cachedBuffer, 0, this.cachedLength, TraceLogConfig.DEFAULT_CHARSET);
        } catch (Exception e) {
            this.content = TraceLogConfig.BODY_PARSE_FAIL;
        }
    }

    @Override
    public void close() throws IOException {
        this.flush();
        this.outputStream.close();
        this.cachedBuffer = null;
    }
}
