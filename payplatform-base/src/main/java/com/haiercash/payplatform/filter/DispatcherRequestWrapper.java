package com.haiercash.payplatform.filter;

import com.bestvike.collection.ArrayUtils;
import com.bestvike.collection.EnumerationUtils;
import com.bestvike.collection.IteratorUtils;
import com.bestvike.lang.StringUtils;
import com.bestvike.linq.Linq;
import com.haiercash.payplatform.diagnostics.TraceID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.List;

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
        this.init();
    }

    protected void init() {
        String value = super.getHeader(TraceID.NAME);
        this.traceID = StringUtils.isEmpty(value) ? TraceID.generate() : value;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> headerNames = EnumerationUtils.toList(super.getHeaderNames());
        boolean contains = Linq.asEnumerable(headerNames).any(headerName -> StringUtils.equalsIgnoreCase(headerName, TraceID.NAME));
        if (!contains)
            headerNames.add(TraceID.NAME);
        return IteratorUtils.toEnumeration(headerNames.iterator());
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
