package com.haiercash.payplatform.config;

import com.haiercash.spring.rabbit.RabbitInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-10-07.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.cmis")
public class CmisConfig {
    private String url;
    private RabbitInfo rabbit;
}
