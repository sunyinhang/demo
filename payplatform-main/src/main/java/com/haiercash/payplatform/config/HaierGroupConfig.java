package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.haierGroup")
public class HaierGroupConfig {
    private String accountUrl;
}
