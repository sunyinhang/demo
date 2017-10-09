package com.haiercash.payplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * eureka server list.
 *
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Configuration
@ConfigurationProperties(prefix = "app.rest")
public class EurekaServer {
    public static String APPCA;
    public static String APPMSG;
    public static String APPMANAGE;
    public static String CMISPROXY;
    public static String CMISFRONTSERVER;
    public static String CRM;
    public static String GM;
    public static String UAUTH;
    public static String HCPORTAL;
    public static String ACQUIRER;
    public static String ORDER;
    public static String APPSERVER;
    public static String APPSERVERNOAUTH;
    public static String APPSERVERNOAUTHNEW;
}
