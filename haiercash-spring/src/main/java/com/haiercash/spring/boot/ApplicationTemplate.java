package com.haiercash.spring.boot;

import org.springframework.context.ApplicationContext;

/**
 * Created by 许崇雷 on 2017-11-22.
 */
public final class ApplicationTemplate {
    private final ApplicationContext context;
    private final ApplicationProperties properties;

    public ApplicationTemplate(ApplicationContext context, ApplicationProperties properties) {
        this.context = context;
        this.properties = properties;
    }

    //通过name获取 Bean.
    public Object getBean(String name) {
        return this.context.getBean(name);
    }

    //通过class获取Bean.
    public <T> T getBean(Class<T> clazz) {
        return this.context.getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public <T> T getBean(String name, Class<T> clazz) {
        return this.context.getBean(name, clazz);
    }

    //属性
    public ApplicationProperties getProperties() {
        return properties;
    }
}
