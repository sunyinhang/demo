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
    private static RestTemplate restTemplateJson;
    private static RestTemplate restTemplateXml;
    @Autowired
    private RestTemplate restTemplateInstance;
    @Autowired
    @Qualifier("restTemplateJson")
    private RestTemplate restTemplateJsonInstance;
    @Autowired
    @Qualifier("restTemplateXml")
    private RestTemplate restTemplateXmlInstance;

    private RestTemplateProvider() {
    }

    //负载均衡的
    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    //非负载均衡 Json
    public static RestTemplate getRestTemplateJson() {
        return restTemplateJson;
    }

    //非负载均衡 Xml
    public static RestTemplate getRestTemplateXml() {
        return restTemplateXml;
    }

    @PostConstruct
    private void init() {
        restTemplate = this.restTemplateInstance;
        restTemplateJson = this.restTemplateJsonInstance;
        restTemplateXml = this.restTemplateXmlInstance;
    }
}
