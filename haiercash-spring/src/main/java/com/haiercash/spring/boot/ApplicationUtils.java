package com.haiercash.spring.boot;

/**
 * Created by 许崇雷 on 2017-11-22.
 */
public class ApplicationUtils {
    private ApplicationUtils() {
    }

    private static ApplicationTemplate getApplicationTemplate() {
        return ApplicationProvider.getApplicationTemplate();
    }

    public static Object getBean(String name) {
        return getApplicationTemplate().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationTemplate().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationTemplate().getBean(name, clazz);
    }

    public static ApplicationProperties getProperties() {
        return getApplicationTemplate().getProperties();
    }
}
