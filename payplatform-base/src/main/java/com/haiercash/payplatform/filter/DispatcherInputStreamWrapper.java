package com.haiercash.payplatform.filter;

import com.bestvike.io.CharsetNames;
import org.springframework.util.Assert;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class DispatcherInputStreamWrapper extends ServletInputStream {
    private static final int MAX_CACHE = 1024 * 8;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final String BODY_PARSE_FAIL = "内容转换为字符串失败";
    private static final String BODY_HAS_MORE = "(...内容过大，无法显示)";
    private static final Charset DEFAULT_CHARSET = Charset.forName(CharsetNames.UTF_8);
    private final ServletInputStream inputStream;
    private byte[] cachedBuffer;
    private int cachedIndex;
    private String content;

    public DispatcherInputStreamWrapper(ServletInputStream inputStream) {
        Assert.notNull(inputStream, "inputStream can not be null.");
        this.inputStream = inputStream;
        this.cacheStream();
        this.parseBody();
    }

    protected void cacheStream() {
        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int readed;
                while ((readed = this.inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    outputStream.write(buffer, 0, readed);
                    if (outputStream.size() >= MAX_CACHE)
                        break;
                }
                this.cachedBuffer = outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void parseBody() {
        try {
            this.content = this.inputStream.isFinished()
                    ? new String(this.cachedBuffer, DEFAULT_CHARSET)
                    : new String(this.cachedBuffer, DEFAULT_CHARSET) + BODY_HAS_MORE;
        } catch (Exception e) {
            this.content = BODY_PARSE_FAIL;
        }
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public int read() throws IOException {
        return this.cachedIndex == this.cachedBuffer.length
                ? this.inputStream.read()
                : (this.cachedBuffer[this.cachedIndex++] & 255);
    }

    @Override
    public boolean isFinished() {
        return this.cachedIndex == this.cachedBuffer.length && this.inputStream.isFinished();
    }

    @Override
    public boolean isReady() {
        return this.inputStream.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        this.inputStream.setReadListener(readListener);
    }
}
