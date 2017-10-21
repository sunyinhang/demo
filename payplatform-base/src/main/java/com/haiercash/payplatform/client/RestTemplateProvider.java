package com.haiercash.payplatform.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Component
public final class RestTemplateProvider {
    private static RestTemplate restTemplate;
    private static RestTemplate restTemplateNormal;
    @Autowired
    private RestTemplate restTemplateInstance;
    @Autowired
    @Qualifier("restTemplateNormal")
    private RestTemplate restTemplateNormalInstance;

    //负载均衡的
    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    //非负载均衡的
    public static RestTemplate getRestTemplateNormal() {
        return restTemplateNormal;
    }

    @PostConstruct
    private void init() {
        restTemplate = this.restTemplateInstance;
        restTemplateNormal = this.restTemplateNormalInstance;
    }
}
