package com.haiercash.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 许崇雷 on 2017-11-22.
 */
@Data
@ConfigurationProperties(prefix = "spring.application")
public final class ApplicationProperties {
    private String name;
    private String description;
    private String version;
}
