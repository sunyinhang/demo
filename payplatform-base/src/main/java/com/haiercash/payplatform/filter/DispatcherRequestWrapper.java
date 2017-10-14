package com.haiercash.payplatform.filter;

import com.bestvike.collection.ArrayUtils;
import com.bestvike.collection.EnumerationUtils;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.diagnostics.TraceID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;

/**
 * 请求包装器
 *
 * @author 许崇雷
 * @date 2017/6/29
 */
final class DispatcherRequestWrapper extends HttpServletRequestWrapper {
    private String traceID;

    DispatcherRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return EnumerationUtils.append(super.getHeaderNames(), TraceID.NAME);
    }

    @Override
    public String getHeader(String name) {
        if (StringUtils.equalsIgnoreCase(name, TraceID.NAME)) {
            String value = super.getHeader(TraceID.NAME);
            if (StringUtils.isNotEmpty(value))
                return value;
            if (StringUtils.isNotEmpty(this.traceID))
                return this.traceID;
            return this.traceID = TraceID.generate();
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (StringUtils.equalsIgnoreCase(name, TraceID.NAME)) {
            String traceID = this.getHeader(TraceID.NAME);
            return ArrayUtils.asEnumeration(traceID);
        }
        return super.getHeaders(name);
    }
}
