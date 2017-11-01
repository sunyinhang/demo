package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-10-07.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.cmis")
public class AppCmisConfig {
    private String url;
}
