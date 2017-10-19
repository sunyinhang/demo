package com.haiercash.payplatform.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class DispatcherResponseWrapper extends HttpServletResponseWrapper {
    private DispatcherOutputStreamWrapper outputStream;

    public DispatcherResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public DispatcherOutputStreamWrapper getOutputStream() throws IOException {
        if (this.outputStream == null)
            this.outputStream = new DispatcherOutputStreamWrapper(super.getOutputStream());
        return this.outputStream;
    }
}
