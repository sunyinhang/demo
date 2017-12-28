package com.haiercash.core.http;

import com.haiercash.core.lang.NumberUtils;
import com.haiercash.core.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Request 扩展
 *
 * @author 许崇雷
 * @date 2017/6/2
 */
public final class ServletUtils {
    //未知
    private static final String UNKNOWN = "unknown";

    //ip 有效?
    private static boolean isValid(String ip) {
        return !isNotValid(ip);
    }

    //ip 无效?
    private static boolean isNotValid(String ip) {
        return StringUtils.isEmpty(ip) || StringUtils.equalsIgnoreCase(ip, UNKNOWN);
    }

    //获取多个 ip 连接的字符串.格式 unknown,1.1.2.2,unknown,2.1.2.1
    private static String getIps(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (isValid(xForwardedFor))
            return xForwardedFor;

        String proxyClientIP = request.getHeader("Proxy-Client-IP");
        if (isValid(proxyClientIP))
            return proxyClientIP;

        String wlProxyClientIP = request.getHeader("WL-Proxy-Client-IP");
        if (isValid(wlProxyClientIP))
            return wlProxyClientIP;

        String httpClientIP = request.getHeader("HTTP_CLIENT_IP");
        if (isValid(httpClientIP))
            return httpClientIP;

        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValid(httpXForwardedFor))
            return httpXForwardedFor;

        return request.getRemoteAddr();
    }

    /**
     * 获取客户端真实 ip
     *
     * @param request 请求
     * @return ip 地址
     */
    public static String getIp(HttpServletRequest request) {
        String ips = getIps(request);
        if (isNotValid(ips))
            return ips;
        String[] arr = StringUtils.split(ips, new char[]{','}, true);
        for (String ip : arr) {
            if (isValid(ip))
                return ip;
        }
        return UNKNOWN;
    }

    /**
     * 获取客户端端口
     *
     * @param request 请求
     * @return 端口
     */
    public static int getPort(HttpServletRequest request) {
        String port = request.getHeader("remote-port");
        return NumberUtils.toInt(port);
    }
}
