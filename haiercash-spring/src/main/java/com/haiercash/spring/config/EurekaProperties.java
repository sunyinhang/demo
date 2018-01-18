package com.haiercash.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
@Data
@ConfigurationProperties(prefix = "app.rest")
public final class EurekaProperties {
    private String ACQUIRER;
    private String APPCA;
    private String APPMANAGE;
    private String APPMSG;
    private String APPSERVER;
    private String APPSERVERNOAUTHNEW;
    private String CMISFRONTSERVER;
    private String CMISINTERFACESERVER;
    private String CMISPROXY;
    private String CRM;
    private String GM;
    private String HCPORTAL;
    private String ORDER;
    private String OUTREACHPLATFORM;
    private String UAUTH;
}
