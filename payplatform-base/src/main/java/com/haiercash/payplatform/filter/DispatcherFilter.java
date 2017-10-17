package com.haiercash.payplatform.filter;

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
 *
 * @author 许崇雷
 * @date 2017/6/29
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class DispatcherFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //转发 (forward) 保持原始上下文
        if (RequestContext.exists()) {
            chain.doFilter(request, response);
            return;
        }

        //非转发 (not forward)
        DispatcherRequestWrapper httpRequest = new DispatcherRequestWrapper((HttpServletRequest) request);
        DispatcherResponseWrapper httpResponse = new DispatcherResponseWrapper((HttpServletResponse) response);
        RequestContext.begin(httpRequest, httpResponse);
        IncomingLog.writeRequestLog(httpRequest);
        long begin = System.currentTimeMillis();
        try {
            chain.doFilter(httpRequest, httpResponse);
        } catch (Exception e) {
            IncomingLog.writeError(httpRequest, e, System.currentTimeMillis() - begin);
            throw e;
        } finally {
            IncomingLog.writeResponseLog(httpRequest, httpResponse, System.currentTimeMillis() - begin);
            RequestContext.end();
        }
    }

    @Override
    public void destroy() {
    }
}
