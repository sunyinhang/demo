package com.haiercash.spring.boot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-22.
 */
@Component(ApplicationProvider.BEAN_NAME)
public final class ApplicationProvider {
    public static final String BEAN_NAME = "applicationProvider";
    private static ApplicationTemplate applicationTemplate;
    private final Log log = LogFactory.getLog(ApplicationProvider.class);
    @Autowired
    private ApplicationTemplate applicationTemplateInstance;

    public static ApplicationTemplate getApplicationTemplate() {
        return applicationTemplate;
    }

    @PostConstruct
    private void init() {
        applicationTemplate = this.applicationTemplateInstance;
        ApplicationProperties properties = applicationTemplate.getProperties();
        log.info(String.format("Application name: %s, description: %s, version: %s", properties.getName(), properties.getDescription(), properties.getVersion()));
    }
}
