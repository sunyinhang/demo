package com.haiercash.spring.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    @ConditionalOnMissingBean
    ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        if (this.restProperties.getConnectTimeout() != null)
            simpleClientHttpRequestFactory.setConnectTimeout(this.restProperties.getConnectTimeout());
        if (this.restProperties.getReadTimeout() != null)
            simpleClientHttpRequestFactory.setReadTimeout(this.restProperties.getReadTimeout());
        return simpleClientHttpRequestFactory;
    }

    @Bean
    @Primary
    @LoadBalanced
    @ConditionalOnMissingBean(name = "restTemplate")
    RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateEx restTemplate = new RestTemplateEx(RestTemplateSupportedType.JSON, clientHttpRequestFactory);
        this.httpConvertersProperties.config(restTemplate);
        this.restProperties.config(restTemplate);
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(name = "restTemplateJson")
    RestTemplate restTemplateJson(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateEx restTemplate = new RestTemplateEx(RestTemplateSupportedType.JSON, clientHttpRequestFactory);
        this.httpConvertersProperties.config(restTemplate);
        this.restProperties.config(restTemplate);
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(name = "restTemplateXml")
    RestTemplate restTemplateXml(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateEx restTemplate = new RestTemplateEx(RestTemplateSupportedType.XML, clientHttpRequestFactory);
        this.httpConvertersProperties.config(restTemplate);
        this.restProperties.config(restTemplate);
        return restTemplate;
    }
}
