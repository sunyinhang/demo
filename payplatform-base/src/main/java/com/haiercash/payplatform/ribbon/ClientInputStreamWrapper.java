package com.haiercash.payplatform.ribbon;

import com.bestvike.io.CharsetNames;
import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientInputStreamWrapper extends InputStream {
    private static final int MAX_CACHE = 1024 * 128;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final String BODY_PARSE_FAIL = "内容转换为字符串失败";
    private static final String BODY_HAS_MORE = "(...内容过大，无法显示)";
    private static final Charset DEFAULT_CHARSET = Charset.forName(CharsetNames.UTF_8);
    private final ClientHttpResponse response;
    private final InputStream inputStream;
    private byte[] cachedBuffer;
    private int cachedIndex;
    private String content;
    private boolean hasMore;

    public ClientInputStreamWrapper(ClientHttpResponse response, InputStream inputStream) {
        Assert.notNull(response, "response can not be null.");
        Assert.notNull(inputStream, "inputStream can not be null.");
        this.response = response;
        this.inputStream = inputStream;
        this.cacheStream();
        this.parseBody();
    }

    protected void cacheStream() {
        try {
            if (this.response.getHeaders().getContentLength() > MAX_CACHE) {
                this.cachedBuffer = IOUtils.toByteArray(this.inputStream, MAX_CACHE);
                return;
            }
            try (ByteArrayOutputStream mem = new ByteArrayOutputStream(BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int readed;
                while ((readed = this.inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    mem.write(buffer, 0, readed);
                    if (mem.size() >= MAX_CACHE) {
                        this.hasMore = true;
                        break;
                    }
                }
                this.cachedBuffer = mem.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void parseBody() {
        try {
            this.content = this.hasMore
                    ? new String(this.cachedBuffer, DEFAULT_CHARSET) + BODY_HAS_MORE
                    : new String(this.cachedBuffer, DEFAULT_CHARSET);
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
}
