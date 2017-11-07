package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-10-31.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.other")
public class AppOtherConfig {
    private String haierDataImg_url;
    private String outplatform_url;
    private String face_DataImg_url;
    private String appServer_page_url;
    private String haierData_url;
    private String haiercashpay_web_url;
    private String haiershunguang_ts_url;
    private String moxie_apikey;
}
