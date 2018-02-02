package com.haiercash.spring.context;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.threading.InheritThreadLocal;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 线程上下文
 * Created by 许崇雷 on 2017-7-1.
 */
public final class RequestContext {
    //线程本地存储
    private static final ThreadLocal<RequestContextData> contexts = InheritThreadLocal.withInitial(RequestContextData::new);

    //region property

    public static boolean exists() {
        return contexts.get().exists;
    }

    public static HttpServletRequest getRequest() {
        return contexts.get().request;
    }

    public static HttpServletResponse getResponse() {
        return contexts.get().response;
    }

    public static ServletContext getServletContext() {
        return contexts.get().servletContext;
    }

    public static HttpSession getSession() {
        return contexts.get().session;
    }

    public static Map<String, Cookie> getCookies() {
        return contexts.get().cookies;
    }

    //endregion

    public static void init(HttpServletRequest request, HttpServletResponse response) {
        RequestContextData data = contexts.get();
        data.exists = true;
        data.request = request;
        data.response = response;
        data.servletContext = request.getServletContext();
        data.session = request.getSession(false);
        Cookie[] cookies = request.getCookies();
        data.cookies = cookies == null ? MapUtils.EMPTY_MAP : Arrays.stream(cookies).collect(Collectors.toMap(Cookie::getName, item -> item));
    }

    public static void reset() {
        RequestContextData data = contexts.get();
        data.exists = false;
        data.request = null;
        data.response = null;
        data.servletContext = null;
        data.session = null;
        data.cookies = null;
    }

    private static final class RequestContextData {
        private boolean exists;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private ServletContext servletContext;
        private HttpSession session;
        private Map<String, Cookie> cookies;
    }
}
