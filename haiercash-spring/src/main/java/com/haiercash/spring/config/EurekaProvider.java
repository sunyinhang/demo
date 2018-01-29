package com.haiercash.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2018-01-29.
 */
@Component(EurekaProvider.BEAN_NAME)
public final class EurekaProvider {
    public static final String BEAN_NAME = "eurekaProvider";
    private static EurekaProperties eurekaProperties;
    @Autowired
    private EurekaProperties eurekaPropertiesInstance;

    public static EurekaProperties getEurekaProperties() {
        return eurekaProperties;
    }

    @PostConstruct
    private void init() {
        eurekaProperties = this.eurekaPropertiesInstance;
    }
}
