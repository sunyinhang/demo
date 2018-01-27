package com.haiercash.spring.weixin;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
@Configuration
@EnableConfigurationProperties(WeiXinProperties.class)
public class WeiXinAutoConfiguration {
    private final WeiXinProperties properties;

    public WeiXinAutoConfiguration(WeiXinProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    WeiXinApi weiXinApi() {
        return new WeiXinApi(this.properties);
    }
}
