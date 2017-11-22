package com.haiercash.spring.boot;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-11-22.
 */
@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationAutoConfiguration {
    private final ApplicationContext context;
    private final ApplicationProperties properties;

    public ApplicationAutoConfiguration(ApplicationContext context, ApplicationProperties properties) {
        this.context = context;
        this.properties = properties;
    }

    @Bean
    ApplicationTemplate applicationTemplate() {
        return new ApplicationTemplate(this.context, this.properties);
    }
}
