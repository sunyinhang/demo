package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by 许崇雷 on 2017-10-07.
 */
@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:config/cmisConfig.yml")
public class CmisConfig {
    private String url;
}
