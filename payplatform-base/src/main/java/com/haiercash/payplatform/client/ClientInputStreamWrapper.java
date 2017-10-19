package com.haiercash.payplatform.client;

import com.bestvike.io.CharsetNames;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientInputStreamWrapper extends InputStream {
    private static final int MAX_DISPLAY = 1024 * 2;//最大显示长度,必须小于缓冲区大小
    private static final int BUFFER_SIZE = 1024 * 4;//缓冲区大小
    private static final String BODY_PARSE_FAIL = "内容转换为字符串失败";
    private static final String BODY_HAS_MORE = "(...内容过大，无法显示)";
    private static final Charset DEFAULT_CHARSET = Charset.forName(CharsetNames.UTF_8);
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
            int readed;
            this.cachedBuffer = new byte[BUFFER_SIZE];
            while ((readed = this.inputStream.read(this.cachedBuffer, this.cachedLength, BUFFER_SIZE - this.cachedLength)) > 0)
                this.cachedLength += readed;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseBody() {
        try {
            this.content = this.cachedLength > MAX_DISPLAY
                    ? (new String(this.cachedBuffer, 0, MAX_DISPLAY, DEFAULT_CHARSET) + BODY_HAS_MORE)
                    : new String(this.cachedBuffer, 0, this.cachedLength, DEFAULT_CHARSET);
        } catch (Exception e) {
            this.content = BODY_PARSE_FAIL;
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
}
