package com.haiercash.spring.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by 许崇雷 on 2017-10-16.
 */
public final class DispatcherResponseWrapper extends HttpServletResponseWrapper {
    private DispatcherOutputStreamWrapper outputStream;
    private boolean usingWriter;

    public DispatcherResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public boolean isUsingWriter() {
        return this.usingWriter;
    }

    @Override
    public DispatcherOutputStreamWrapper getOutputStream() throws IOException {
        if (this.outputStream == null)
            this.outputStream = new DispatcherOutputStreamWrapper(super.getOutputStream());
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        this.usingWriter = true;
        return super.getWriter();
    }
}
