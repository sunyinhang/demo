package com.haiercash.payplatform.ribbon;

import com.bestvike.io.CharsetNames;
import com.bestvike.lang.StringUtils;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class ClientOutputStreamWrapper extends OutputStream {
    private static final long MAX_CACHE = 1024 * 8;
    private static final String BODY_CONVERT_FAIL = "内容转换为字符串失败";
    private static final String BODY_HAS_MORE = "(...内容过大，无法显示)";
    private static final String DEFAULT_CHARSET = CharsetNames.UTF_8;
    private final OutputStream outputStream;
    private ByteArrayOutputStream cachedStream;
    private String content;
    private boolean hasMore;

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
        if (this.cachedStream == null) {
            this.cachedStream = new ByteArrayOutputStream();
        }
        if (this.cachedStream.size() >= MAX_CACHE) {
            this.hasMore = true;
            return;
        }
        this.cachedStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
        //已经 flush
        if (this.content != null)
            return;
        if (this.cachedStream == null) {
            this.content = StringUtils.EMPTY;
            return;
        }
        try {
            this.content = this.hasMore
                    ? (this.cachedStream.toString(DEFAULT_CHARSET) + BODY_HAS_MORE)
                    : this.cachedStream.toString(DEFAULT_CHARSET);
        } catch (Exception e) {
            this.content = BODY_CONVERT_FAIL;
        }
    }

    @Override
    public void close() throws IOException {
        this.flush();
        this.outputStream.close();
        if (this.cachedStream != null) {
            this.cachedStream.close();
            this.cachedStream = null;
        }
    }
}
