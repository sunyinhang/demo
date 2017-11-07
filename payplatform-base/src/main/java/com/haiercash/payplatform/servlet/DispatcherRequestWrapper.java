package com.haiercash.payplatform.servlet;

import com.bestvike.linq.exception.NotSupportedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 请求包装器
 *
 * @author 许崇雷
 * @date 2017/6/29
 */
public final class DispatcherRequestWrapper extends HttpServletRequestWrapper {
    private DispatcherInputStreamWrapper inputStream;

    public DispatcherRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public DispatcherInputStreamWrapper getInputStream() throws IOException {
        if (this.inputStream == null)
            this.inputStream = new DispatcherInputStreamWrapper(super.getInputStream());
        return this.inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new NotSupportedException("not support read request by reader");
    }
}
