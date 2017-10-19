package com.haiercash.payplatform.servlet;

import com.haiercash.payplatform.context.RequestContext;
import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.diagnostics.IncomingLog;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 过滤器
 * Created by 许崇雷 on 2017/6/29.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class DispatcherFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //转发 (forward) 保持原始上下文
        if (RequestContext.exists()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //非转发 (not forward)
        DispatcherRequestWrapper request = new DispatcherRequestWrapper((HttpServletRequest) servletRequest);
        DispatcherResponseWrapper response = new DispatcherResponseWrapper((HttpServletResponse) servletResponse);
        RequestContext.init(request, response);
        ThreadContext.init(request.getHeader("token"), request.getHeader("channel"), request.getHeader("channelno"));
        long begin = System.currentTimeMillis();
        IncomingLog.writeRequestLog(request);
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            IncomingLog.writeError(request, e, System.currentTimeMillis() - begin);
            throw e;
        } finally {
            IncomingLog.writeResponseLog(request, response, System.currentTimeMillis() - begin);
            ThreadContext.reset();
            RequestContext.reset();
        }
    }

    @Override
    public void destroy() {
    }
}
