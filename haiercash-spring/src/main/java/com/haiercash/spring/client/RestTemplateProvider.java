package com.haiercash.spring.client;

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
    private static RestTemplate ribbonJsonRestTemplate;
    private static RestTemplate ribbonXmlRestTemplate;
    private static RestTemplate jsonRestTemplate;
    private static RestTemplate xmlRestTemplate;
    @Autowired
    private RestTemplate ribbonJsonRestTemplateInstance;
    @Autowired
    @Qualifier("ribbonXmlRestTemplate")
    private RestTemplate ribbonXmlRestTemplateInstance;
    @Autowired
    @Qualifier("jsonRestTemplate")
    private RestTemplate jsonRestTemplateInstance;
    @Autowired
    @Qualifier("xmlRestTemplate")
    private RestTemplate xmlRestTemplateInstance;

    private RestTemplateProvider() {
    }

    //负载均衡 Json
    public static RestTemplate getRibbonJsonRestTemplate() {
        return ribbonJsonRestTemplate;
    }

    //负载均衡 Xml
    public static RestTemplate getRibbonXmlRestTemplate() {
        return ribbonXmlRestTemplate;
    }

    //非负载均衡 Json
    public static RestTemplate getJsonRestTemplate() {
        return jsonRestTemplate;
    }

    //非负载均衡 Xml
    public static RestTemplate getXmlRestTemplate() {
        return xmlRestTemplate;
    }

    @PostConstruct
    private void init() {
        ribbonJsonRestTemplate = this.ribbonJsonRestTemplateInstance;
        ribbonXmlRestTemplate = this.ribbonXmlRestTemplateInstance;
        jsonRestTemplate = this.jsonRestTemplateInstance;
        xmlRestTemplate = this.xmlRestTemplateInstance;
    }
}
