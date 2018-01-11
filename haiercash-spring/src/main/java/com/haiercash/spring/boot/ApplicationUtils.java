package com.haiercash.spring.boot;

import com.haiercash.core.lang.StringUtils;

/**
 * Created by 许崇雷 on 2017-11-22.
 */
public final class ApplicationUtils {
    private static final ApplicationProperties EMPTY_PROPERTIES;

    static {
        EMPTY_PROPERTIES = new ApplicationProperties();
        EMPTY_PROPERTIES.setName(StringUtils.EMPTY);
        EMPTY_PROPERTIES.setDescription(StringUtils.EMPTY);
        EMPTY_PROPERTIES.setVersion(StringUtils.EMPTY);
    }

    private ApplicationUtils() {
    }

    private static ApplicationTemplate getApplicationTemplate() {
        return ApplicationProvider.getApplicationTemplate();
    }

    public static Object getBean(String name) {
        ApplicationTemplate applicationTemplate = getApplicationTemplate();
        return applicationTemplate == null ? null : applicationTemplate.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        ApplicationTemplate applicationTemplate = getApplicationTemplate();
        return applicationTemplate == null ? null : applicationTemplate.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        ApplicationTemplate applicationTemplate = getApplicationTemplate();
        return applicationTemplate == null ? null : applicationTemplate.getBean(name, clazz);
    }

    public static ApplicationProperties getProperties() {
        ApplicationTemplate applicationTemplate = getApplicationTemplate();
        return applicationTemplate == null ? EMPTY_PROPERTIES : applicationTemplate.getProperties();
    }
}
