package com.haiercash.payplatform.filter;

import com.bestvike.collection.ArrayUtils;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.diagnostics.TraceID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 请求包装器
 *
 * @author 许崇雷
 * @date 2017/6/29
 */
public final class DispatcherRequestWrapper extends HttpServletRequestWrapper {
    private String traceID;
    private DispatcherInputStreamWrapper inputStream;

    public DispatcherRequestWrapper(HttpServletRequest request) {
        super(request);
        this.init();
    }

    protected void init() {
        String value = super.getHeader(TraceID.NAME);
        this.traceID = StringUtils.isEmpty(value) ? TraceID.generate() : value;
    }

    @Override
    public DispatcherInputStreamWrapper getInputStream() throws IOException {
        if (this.inputStream == null)
            this.inputStream = new DispatcherInputStreamWrapper(this, super.getInputStream());
        return this.inputStream;
    }

    @Override
    public String getHeader(String name) {
        return StringUtils.equalsIgnoreCase(name, TraceID.NAME) ? this.traceID : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return StringUtils.equalsIgnoreCase(name, TraceID.NAME) ? ArrayUtils.asEnumeration(this.traceID) : super.getHeaders(name);
    }
}
