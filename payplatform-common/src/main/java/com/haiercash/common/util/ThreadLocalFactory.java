package com.haiercash.common.util;

/**
 * 获取ThreadLocal工厂类
 * Created by zhouwushuang on 2017.04.20.
 */
public class ThreadLocalFactory {
    private static ThreadLocal threadLocal = new ThreadLocal();
    public static ThreadLocal getThreadLocal() {
        return threadLocal;
    }
}
