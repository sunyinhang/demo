package com.haiercash.payplatform.servlet;

import com.bestvike.io.CharsetNames;
import com.bestvike.lang.StringUtils;
import org.springframework.util.Assert;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class DispatcherOutputStreamWrapper extends ServletOutputStream {
    private static final int MAX_DISPLAY = 1024;//最大显示长度
    private static final String BODY_CONVERT_FAIL = "内容转换为字符串失败";
    private static final String BODY_HAS_MORE = "(...内容过大，无法显示)";
    private static final String DEFAULT_CHARSET = CharsetNames.UTF_8;
    private final ServletOutputStream outputStream;
    private ByteArrayOutputStream cachedStream;
    private String content;
    private boolean hasMore;

    public DispatcherOutputStreamWrapper(ServletOutputStream outputStream) {
        Assert.notNull(outputStream, "outputStream can not be null.");
        this.outputStream = outputStream;
    }

    public String getContent() {
        return this.content;
    }

    @Override
    public boolean isReady() {
        return this.outputStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        this.outputStream.setWriteListener(listener);
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
        if (this.cachedStream == null)
            this.cachedStream = new ByteArrayOutputStream(MAX_DISPLAY);
        if (this.cachedStream.size() >= MAX_DISPLAY) {
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
