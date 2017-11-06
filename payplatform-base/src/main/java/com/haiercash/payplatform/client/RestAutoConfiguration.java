package com.haiercash.payplatform.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Created by 许崇雷 on 2017-11-03.
 */
@Configuration
@EnableConfigurationProperties({HttpConvertersProperties.class, RestProperties.class})
public class RestAutoConfiguration {
    private final HttpConvertersProperties httpConvertersProperties;
    private final RestProperties restProperties;

    public RestAutoConfiguration(HttpConvertersProperties httpConvertersProperties, RestProperties restProperties) {
        this.httpConvertersProperties = httpConvertersProperties;
        this.restProperties = restProperties;
    }

    @Bean
    @Primary
    @LoadBalanced
    @ConditionalOnMissingBean(name = "restTemplate")
    RestTemplate restTemplate() {
        RestTemplateEx restTemplate = new RestTemplateEx(RestTemplateSupportedType.JSON);
        this.httpConvertersProperties.config(restTemplate);
        this.restProperties.config(restTemplate);
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(name = "restTemplateJson")
    RestTemplate restTemplateJson() {
        RestTemplateEx restTemplate = new RestTemplateEx(RestTemplateSupportedType.JSON);
        this.httpConvertersProperties.config(restTemplate);
        this.restProperties.config(restTemplate);
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(name = "restTemplateXml")
    RestTemplate restTemplateXml() {
        RestTemplateEx restTemplate = new RestTemplateEx(RestTemplateSupportedType.XML);
        this.httpConvertersProperties.config(restTemplate);
        this.restProperties.config(restTemplate);
        return restTemplate;
    }
}
