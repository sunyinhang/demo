package com.haiercash.payplatform.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by 许崇雷 on 2017-10-11.
 */
@Component
public final class ApplicationContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private Log log = LogFactory.getLog(ApplicationContextUtils.class);

    //获取applicationContext
    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (applicationContext == null)
            return;
        if (ApplicationContextUtils.applicationContext != null)
            return;
        ApplicationContextUtils.applicationContext = applicationContext;
        log.info("ApplicationContextUtils inited");
    }

    //通过name获取 Bean.
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}
