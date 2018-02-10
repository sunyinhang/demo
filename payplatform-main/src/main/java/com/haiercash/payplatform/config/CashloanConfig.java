package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-10-18.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.cashloan")
public class CashloanConfig {
    private List<String> whiteTagIds;
    private String iserviceTagId;
    private String days;
}
