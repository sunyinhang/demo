package com.haiercash.spring.servlet;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.context.RequestContext;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.context.TraceContext;
import com.haiercash.spring.trace.rest.IncomingLog;
import org.apache.catalina.connector.ClientAbortException;
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
    private static final String NAME_TOKEN = "token";
    private static final String NAME_CHANNEL = "channel";
    private static final String NAME_CHANNEL_NO_PRIMARY = "channel_no";
    private static final String NAME_CHANNEL_NO_SECONDARY = "channelNo";

    private String getArg(DispatcherRequestWrapper request, String name) {
        return StringUtils.defaultIfEmpty(request.getHeader(name), request.getParameter(name));
    }

    @Override
    public void init(FilterConfig filterConfig) {
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
        ThreadContext.init(this.getArg(request, NAME_TOKEN), this.getArg(request, NAME_CHANNEL), Convert.defaultString(this.getArg(request, NAME_CHANNEL_NO_PRIMARY), this.getArg(request, NAME_CHANNEL_NO_SECONDARY)));
        TraceContext.init();
        IncomingLog.writeBeginLog(request);
        long begin = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
            IncomingLog.writeEndLog(request, response, System.currentTimeMillis() - begin);
        } catch (ClientAbortException e) {
            IncomingLog.writeClientAbortErrorLog(request, System.currentTimeMillis() - begin);
        } catch (Exception e) {
            IncomingLog.writeErrorLog(request, e, System.currentTimeMillis() - begin);
        } finally {
            TraceContext.reset();
            ThreadContext.reset();
            RequestContext.reset();
        }
    }

    @Override
    public void destroy() {
    }
}
